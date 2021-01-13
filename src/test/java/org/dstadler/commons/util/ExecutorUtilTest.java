package org.dstadler.commons.util;

import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ExecutorUtilTest {
    private final AtomicReference<Exception> exc = new AtomicReference<>();
    private final CountDownLatch latch = new CountDownLatch(1);

    @Test
    public void testShutdownNoThread() {
        ExecutorService executor = Executors.newSingleThreadExecutor(
                ExecutorUtil.createThreadFactory("ExecutorTest"));

        ThreadTestHelper.assertNoThreadLeft(
                "No thread expected without performing any action on the executor, look at log for thread-dump",
                "ExecutorTest");

        ExecutorUtil.shutdownAndAwaitTermination(executor, 100);

        ThreadTestHelper.assertNoThreadLeft(
                "No thread expected after shutting down the executor, look at log for thread-dump",
                "ExecutorTest");
    }

    @Test
    public void testShutdownWithThread() {
        ExecutorService executor = Executors.newSingleThreadExecutor(
                ExecutorUtil.createThreadFactory("ExecutorTest"));

        executor.submit(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                exc.set(e);
            }
        });

        assertNotNull("There should be a thread running now",
                ExecutorUtil.lookupThread("ExecutorTest"));

        ExecutorUtil.shutdownAndAwaitTermination(executor, 10);

        ThreadTestHelper.assertNoThreadLeft(
                "No thread expected after shutting down the executor, look at log for thread-dump",
                "ExecutorTest");

        assertTrue("Had unexpected exception, only expecting InterruptedException: " + exc.get(),
                exc.get() == null || exc.get() instanceof InterruptedException);
    }

    @Test
    public void testUnexpectedException() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor(
                ExecutorUtil.createThreadFactory("ExecutorTest"));

        executor.submit(() -> {
            try {
                throw new IllegalStateException("test-exception");
            } finally {
                latch.countDown();
            }
        });

        assertNotNull("There should be a thread running now",
                ExecutorUtil.lookupThread("ExecutorTest"));

        // wait for the thread to throw the exception
        latch.await();

        ExecutorUtil.shutdownAndAwaitTermination(executor, 10);

        ThreadTestHelper.assertNoThreadLeft(
                "No thread expected after shutting down the executor, look at log for thread-dump",
                "ExecutorTest");
    }

    @Test
    public void testLookupThreadNotFound() {
        assertNull(ExecutorUtil.lookupThread("unknown running thread"));
    }

    @Test
    public void testLookupThreadFound() throws InterruptedException {
        Thread thread = new Thread(() -> {
            latch.countDown();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                exc.set(e);
            }
        }, "test-thread");
        thread.start();

        latch.await();

        assertSame(thread, ExecutorUtil.lookupThread("test-thread"));

        assertNull("Had unexpected exception: " + exc.get(), exc.get());
    }
}