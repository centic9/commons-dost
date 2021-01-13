package org.dstadler.commons.util;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Some general helper utilitis when working with Executors
 */
public class ExecutorUtil {
    private final static Logger log = LoggerFactory.make();

    /**
     * Creates a simple ThreadFactory which uses the given name-pattern
     * for the threads.
     *
     * Also sets the created threads to "daemon" threads.
     *
     * @return a new ThreadFactory
     */
    public static ThreadFactory createThreadFactory(String namePattern) {
        return new BasicThreadFactory.Builder()
                .daemon(true)
                .namingPattern(namePattern)
                .uncaughtExceptionHandler(
                        (t, e) -> log.log(Level.WARNING, "Unexpected Exception caught in thread " + t.getName(), e))
                .build();
    }

    /**
     * Properly shutdown the {@link ExecutorService} and ensure no tasks
     * are running any more and all threads are stopped.
     *
     * @param executor The ExecutorService to stop.
     * @param timeoutMs How long to wait for tasks to finish
     */
    public static void shutdownAndAwaitTermination(ExecutorService executor, long timeoutMs) {
        // Disable new tasks from being submitted
        executor.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
                // Some jobs did not finish yet => cancel currently executing tasks
                executor.shutdownNow();

                // Wait again for tasks to respond to being cancelled
                if (!executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
                    log.info("Executor did not shutdown cleanly in the given timeout of 10 seconds before cancelling current jobs and 10 seconds after cancelling jobs");
                }
            }
        } catch (@SuppressWarnings("unused") InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Look for a thread where the name contains the given string.
     *
     * If multiple threads are running, one of the matching ones is
     * found without guarantee of which one is returned.
     *
     * @param contains The string to look for in thread-names
     * @return The first thread that is found or null if no thread is running.
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
}
