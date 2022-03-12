package org.dstadler.commons.thread;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.After;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ThreadUtilsTest {
    private final AtomicReference<Throwable> exc = new AtomicReference<>();

    @After
    public void tearDown() {
        if (exc.get() != null) {
            fail("Did not expect an exception but had: " + ExceptionUtils.getStackTrace(exc.get()));
        }

        ThreadTestHelper.assertNoThreadLeft("Had a test-thread still running at the end of testing",
                "TestUtilsTest");
    }

    @Test
    public void testGetThreadsByNameNotFound() {
        final List<Thread> threads = ThreadUtils.getThreadsByName("not existing");
        assertEquals("Had: " + threads,
                0, threads.size());
    }

    @Test
    public void testGetThreadsByNameOne() {
        final List<Thread> threads = ThreadUtils.getThreadsByName(Thread.currentThread().getName());
        assertEquals("Had: " + threads,
                1, threads.size());
    }

    @Test
    public void testGetThreadsByNameMultiple() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(3);
        CountDownLatch latch = new CountDownLatch(1);

        final Thread[] startedThreads = new Thread[3];
        for(int i = 0;i < 3;i++) {
            startedThreads[i] = new Thread(() -> {
                startLatch.countDown();
                try {
                    latch.await();
                } catch (Throwable e) {
                    exc.set(e);
                }
            }, "TestUtilsTest-thread");

            startedThreads[i].start();
        }

        startLatch.await();

        final List<Thread> threads = ThreadUtils.getThreadsByName("TestUtilsTest-thread");
        assertEquals("Had: " + threads,
                3, threads.size());

        // allow threads to finish
        latch.countDown();

        for(int i = 0;i < 3;i++) {
            startedThreads[i].join();
        }

    }

    @Test
    public void testStopStartedThread() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);

        assertNull("No such thread running before",
                ThreadUtils.lookupThread("TestUtilsTest"));

        Thread testThread = new Thread(() -> {
            startLatch.countDown();

            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                // this exception is expected here
            } catch (Throwable e) {
                exc.set(e);
            }
        }, "TestUtilsTest");

        testThread.start();

        startLatch.await();

        Thread thread = ThreadUtils.lookupThread("TestUtilsTest");
        assertNotNull("There is a thread running now",
                thread);
        assertTrue(thread.isAlive());

        ThreadUtils.stopThread(testThread, 1000);

        thread.join();

        assertFalse(thread.isAlive());

        assertNull("No such thread running afterwards",
                ThreadUtils.lookupThread("TestUtilsTest"));
    }

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(ThreadUtils.class);
	}
}