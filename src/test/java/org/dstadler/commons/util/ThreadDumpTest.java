package org.dstadler.commons.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Test;

import org.dstadler.commons.testing.TestHelpers;


public class ThreadDumpTest {

	@Test
	public void testThreadDump() throws Exception {
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
					Object object = new Object();
					synchronized (object) {
						object.wait(200);
					}

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
					Object object = new Object();
					synchronized (object) {
						object.wait(200);
					}
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

		thread.suspend();

		ThreadDump dump = new ThreadDump(true, true);

		TestHelpers.assertContains(dump.toString(), "main", "testthread", "(suspended)");

		thread.resume();

		sem.release(1);
		thread.join();
	}
}
