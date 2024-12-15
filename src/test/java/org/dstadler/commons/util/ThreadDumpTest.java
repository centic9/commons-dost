package org.dstadler.commons.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.Test;


public class ThreadDumpTest {

    @Test
    public void testThreadDump() {
        ThreadDump dump = new ThreadDump(false, false);

        TestHelpers.assertContains(dump.toString(), "main");
    }

    @Test
    public void testThreadDumpWithSync() throws Exception {
        final Semaphore sem = new Semaphore(0);

        Thread thread = new Thread("testthread") {

            @Override
            public void run() {
                try {
                    Thread.sleep(200);

                    sem.acquire();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        };

        thread.start();

        // sleep a bit to let the thread start
        Thread.sleep(100);

        ThreadDump dump = new ThreadDump(true, true);

        TestHelpers.assertContains(dump.toString(), "main", "testthread");

        Thread.sleep(200);
        dump = new ThreadDump(true, true);

        TestHelpers.assertContains(dump.toString(), "main", "testthread");

        sem.release(1);
        thread.join();
    }

    @Test
    public void testThreadDumpWithLocks() throws Exception {
        final ReadWriteLock rw = new ReentrantReadWriteLock();
        rw.writeLock().lock();

        Thread thread = new Thread("testthread") {

            @Override
            public void run() {
                rw.readLock().lock();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                } finally {
                    rw.readLock().unlock();
                }
            }
        };

        thread.start();

        // sleep a bit to let the thread start
        Thread.sleep(100);

        ThreadDump dump = new ThreadDump(true, true);

        TestHelpers.assertContains(dump.toString(), "main", "testthread", "owned by");

        rw.writeLock().unlock();

        thread.join();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testThreadDumpWithSuspended() throws Exception {
        final Semaphore sem = new Semaphore(0);

        Thread thread = new Thread("testthread") {
            @Override
            public void run() {
                try {
                    sem.acquire();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        };

        thread.start();

        // sleep a bit to let the thread start
        Thread.sleep(100);

		boolean suspended = true;
		try {
			thread.suspend();
		} catch (UnsupportedOperationException e) {
			// thrown in JDK 20+
			suspended = false;
		}

        ThreadDump dump = new ThreadDump(true, true);

		TestHelpers.assertContains(dump.toString(), "main", "testthread");
		if (suspended) {
			TestHelpers.assertContains(dump.toString(), "(suspended)");
		} else {
			TestHelpers.assertNotContains(dump.toString(), "(suspended)");
		}

		try {
			thread.resume();
		} catch (UnsupportedOperationException e) {
			// thrown in JDK 20+
		}

        sem.release(1);
        thread.join();
    }
}
