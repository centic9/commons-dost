package org.dstadler.commons.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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