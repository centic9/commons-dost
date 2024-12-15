package org.dstadler.commons.thread;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
public class ThreadUtilsTest {
    private final AtomicReference<Throwable> exc = new AtomicReference<>();

    @AfterEach
    public void tearDown() {
        if (exc.get() != null) {
            fail("Did not expect an exception but had: " + ExceptionUtils.getStackTrace(exc.get()));
        }

        ThreadTestHelper.assertNoThreadLeft("Had a test-thread still running at the end of testing",
                "TestUtilsTest");
    }

    @Test
    public void testGetThreadsByNameNotFound() {
        final Collection<Thread> threads = ThreadUtils.getThreadsByName("not existing");
        assertEquals(0, threads.size(), "Had: " + threads);
    }

    @Test
    public void testGetThreadsByNameOne() {
        final Collection<Thread> threads = ThreadUtils.getThreadsByName(Thread.currentThread().getName());
        assertEquals(1, threads.size(), "Had: " + threads);
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

        final Collection<Thread> threads = ThreadUtils.getThreadsByName("TestUtilsTest-thread");
        assertEquals(3, threads.size(), "Had: " + threads);

        // allow threads to finish
        latch.countDown();

        for(int i = 0;i < 3;i++) {
            startedThreads[i].join();
        }

    }

    @Test
    public void testStopStartedThread() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);

        assertNull(ThreadUtils.lookupThread("TestUtilsTest"),
                "No such thread running before");

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
        assertNotNull(thread,
                "There is a thread running now");
        assertTrue(thread.isAlive());

        ThreadUtils.stopThread(testThread, 1000);

        thread.join();

        assertFalse(thread.isAlive());

        assertNull(ThreadUtils.lookupThread("TestUtilsTest"),
                "No such thread running afterwards");
    }

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(ThreadUtils.class);
	}
}