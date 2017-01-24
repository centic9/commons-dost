package org.dstadler.commons.collections;

import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;


/**
 * All tests from MappedCounterTest are invoked here as well, only with a ConcurrentMappedCounter instance instead
 * to guarantee equal behavior in both implementations
 */
public class ConcurrentMappedCounterTest extends MappedCounterTest {
    private static final int NUMBER_OF_THREADS = 20;
    private static final int NUMBER_OF_TESTS = 1000;

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
            public void doEnd(int threadnum) throws Exception {
                // do stuff at the end ...
            }

            @Override
            public void run(int threadnum, int iter) throws Exception {
                String key = "iter" + iter;
                counter.addInt(key, 1);
                counter.addInt("sum", 1);

                assertTrue(counter.get(key) > 0);
                assertTrue(counter.get("sum") > 0);
                assertTrue(counter.keys().size() > 0);
                assertTrue(counter.entries().size() > 0);

                Map<String, Integer> sortedMap = counter.sortedMap();
                assertTrue(sortedMap.containsKey("sum"));
                assertTrue(sortedMap.containsKey(key));

                assertNotNull(counter);

                // causes tests above to fail... counter.clear();
                assertTrue(counter.sum() > 0);
                assertNotNull(counter.sortedMap());
                assertNotNull(counter.entries());
                assertNotNull(counter.keys());
            }
        });

        assertEquals(NUMBER_OF_THREADS*NUMBER_OF_TESTS, counter.get("sum"));
        assertEquals(NUMBER_OF_THREADS, counter.get("iter1"));

        assertEquals(NUMBER_OF_THREADS*NUMBER_OF_TESTS * 2, counter.sum());
    }

}
