package org.dstadler.commons.thread;

import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.util.SuppressForbidden;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadUtils {
    private final static Logger log = LoggerFactory.make();

    /**
     * Returns a list of all threads with the given name, i.e. equals-match.
     *
     * @param threadName The name to look for.
     * @return A list of found threads. Returns an empty list if no thread is found.
     */
    public static List<Thread> getThreadsByName(String threadName) {
        int count = Thread.currentThread().getThreadGroup().activeCount();

        Thread[] threads = new Thread[count];
        Thread.currentThread().getThreadGroup().enumerate(threads);

        List<Thread> foundThreads = new ArrayList<>();
        for (Thread t : threads) {
            if (t != null && t.getName().equals(threadName)) {
                foundThreads.add(t);
            }
        }
        return foundThreads;
    }

    /**
     * Returns the first thread for which the thread-name contains
     * the given string.
     *
     * The order of searching is undefined, any of the matching
     * threads might be returned.
     *
     * @param contains The string to look for.
     * @return The first matching thread or null if no thread is found.
     */
    public static Thread lookupThread(String contains) {
        int count = Thread.currentThread().getThreadGroup().activeCount();

        Thread[] threads = new Thread[count];
        Thread.currentThread().getThreadGroup().enumerate(threads);

        for (Thread t : threads) {
            if (t != null && t.getName().contains(contains)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Try to wait for the thread to stop gracefully. If it does not
     * stop by itself, first tries to interrupt the thread and after
     * waiting some more, will finally forcefully stop the thread.
     *
     * @param thread The thread to stop
     * @param joinTimeMs How long to wait for the thread to stop
     * @throws InterruptedException If the currently executing thread
     *          was interrupted while waiting
     */
    @SuppressForbidden(reason = "This method calls Thread.stop by definition")
    public static void stopThread(Thread thread, int joinTimeMs) throws InterruptedException {
        // let thread shut down on it's own first
        thread.join(1000);

        // then ensure any blocked operation is interrupted
        thread.interrupt();

        // wait a bit for the thread to actually shut down
        try {
            thread.join(joinTimeMs);
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "Joining thread was interrupted", e);
        }

        // if not stopped yet, forcefully stop it
        try {
            //noinspection deprecation
            thread.stop();
        } catch (ThreadDeath e) {
            log.log(Level.WARNING, "Stopping thread threw an exception", e);
        }
    }
}
