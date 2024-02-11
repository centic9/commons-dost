package org.dstadler.commons.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.jupiter.api.Test;

/**
 * All tests from MappedCounterTest are invoked here as well, only with a ConcurrentMappedCounter instance instead
 * to guarantee equal behavior in both implementations
 */
public class ConcurrentMappedCounterTest extends MappedCounterTest {
    private static final int NUMBER_OF_THREADS = 20;
    private static final int NUMBER_OF_TESTS = 600;

    @Override
    protected <T> MappedCounter<T> createCounter() {
        return new ConcurrentMappedCounter<>();
    }

    @Test
    public void testMultipleThreads() throws Throwable {
        final MappedCounter<String> counter = new ConcurrentMappedCounter<>();

        ThreadTestHelper helper =
            new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

        helper.executeTest(new ThreadTestHelper.TestRunnable() {
            @Override
            public void doEnd(int threadNum) {
                // do stuff at the end ...
            }

            @Override
            public void run(int threadNum, int iter) {
                String key = "iter" + iter;
                counter.add(key, 1);
                counter.add("sum", 1);
                counter.add("r", 15);
                counter.inc("r");
				counter.inc(key);

				counter.remove("r");

				assertTrue(counter.get(key) > 0);
                assertTrue(counter.get("sum") > 0);
				assertFalse(counter.keys().isEmpty());
				assertFalse(counter.entries().isEmpty());

				Map<String, Long> sortedMap = counter.sortedMap();
                assertTrue(sortedMap.containsKey("sum"));
                assertTrue(sortedMap.containsKey(key));

                assertNotNull(counter);

                // causes tests above to fail... counter.clear();
                assertTrue(counter.sum() > 0);
                assertNotNull(counter.sortedMap());
                assertNotNull(counter.entries());
                assertNotNull(counter.keys());

				assertTrue(counter.sum() > 0);
            }
        });

        assertEquals(NUMBER_OF_THREADS*NUMBER_OF_TESTS, counter.get("sum"));
        assertEquals(NUMBER_OF_THREADS*2, counter.get("iter1"));

        assertEquals(NUMBER_OF_THREADS*NUMBER_OF_TESTS * 3, counter.sum());
    }
}
