package org.dstadler.commons.util;

import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

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
    public void testShutdownWithThread() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor(
                ExecutorUtil.createThreadFactory("ExecutorTest"));

        executor.submit(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                exc.set(e);
            }
        });

        assertNotNull(ExecutorUtil.lookupThread("ExecutorTest"),
                "There should be a thread running now");

        ExecutorUtil.shutdownAndAwaitTermination(executor, 50);

        // give the thread some time to stop
        ThreadTestHelper.waitForThreadToFinishSubstring("ExecutorTest", 1000);

        ThreadTestHelper.assertNoThreadLeft(
                "No thread expected after shutting down the executor, look at log for thread-dump",
                "ExecutorTest");

        assertTrue(exc.get() == null || exc.get() instanceof InterruptedException,
                "Had unexpected exception, only expecting InterruptedException: " + exc.get());
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

        assertNotNull(ExecutorUtil.lookupThread("ExecutorTest"),
                "There should be a thread running now");

        // wait for the thread to throw the exception
        latch.await();

        ExecutorUtil.shutdownAndAwaitTermination(executor, 10);

        // allow some time for thread to vanish from the list of threads, sometimes
        // they can "linger" on for a short while...
        ThreadTestHelper.waitForThreadToFinishSubstring("ExecutorTest");

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

        assertNull(exc.get(), "Had unexpected exception: " + exc.get());
    }

	@Test
	public void testThreadFactoryUnhandledException() throws InterruptedException {
		ThreadFactory factory = ExecutorUtil.createThreadFactory("test-thread");
		assertNotNull(factory);

		Thread thread = factory.newThread(() -> {
			throw new IllegalStateException("Test-exception");
		});

		thread.start();
		thread.join();
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(ExecutorUtil.class);
	}
}