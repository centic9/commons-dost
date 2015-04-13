package org.dstadler.commons.collections;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.dstadler.commons.testing.TestHelpers;
import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.Test;



public class ConcurrentMappedCounterTest {
	private static final int NUMBER_OF_THREADS = 20;
	private static final int NUMBER_OF_TESTS = 1000;

	@Test
	public void test() {
		MappedCounter<String> counter = new ConcurrentMappedCounter<>();
		assertEquals(0, counter.get("some"));
		assertNotNull(counter.keys());
		assertNotNull(counter.entries());

		counter.addInt("test", 1);
		assertEquals(1, counter.get("test"));

		counter.addInt("test", 2);
		assertEquals(3, counter.get("test"));

		counter.addInt("other", 14);
		counter.addInt("third", 5);
		assertEquals(3, counter.get("test"));
		assertEquals(14, counter.get("other"));
		assertEquals(5, counter.get("third"));

		Map<String, Integer> sortedMap = counter.sortedMap();
		Iterator<Entry<String, Integer>> iterator = sortedMap.entrySet().iterator();

		// sorted by decreasing value
		assertEquals(Integer.valueOf(14), iterator.next().getValue());
		assertEquals(Integer.valueOf(5), iterator.next().getValue());
		assertEquals(Integer.valueOf(3), iterator.next().getValue());

		assertEquals(22, counter.sum());
	}

	@Test
	public void testGeneric() {
		MappedCounter<Integer> counter = new ConcurrentMappedCounter<>();
		assertEquals(0, counter.get(24));
		assertNotNull(counter.keys());
		assertNotNull(counter.entries());

		counter.addInt(26, 1);
		assertEquals(1, counter.get(26));

		counter.addInt(26, 2);
		assertEquals(3, counter.get(26));

		counter.addInt(88, 14);
		counter.addInt(-3432, 5);
		assertEquals(3, counter.get(26));
		assertEquals(14, counter.get(88));
		assertEquals(5, counter.get(-3432));

		Map<Integer, Integer> sortedMap = counter.sortedMap();
		Iterator<Entry<Integer, Integer>> iterator = sortedMap.entrySet().iterator();

		// sorted by decreasing value
		assertEquals(Integer.valueOf(14), iterator.next().getValue());
		assertEquals(Integer.valueOf(5), iterator.next().getValue());
		assertEquals(Integer.valueOf(3), iterator.next().getValue());
	}

	@Test
	public void testToString() {
		MappedCounter<String> counter = new ConcurrentMappedCounter<>();
		TestHelpers.ToStringTest(counter);

		counter.addInt("Str1", 3);
		counter.addInt("Str4", 6);

		TestHelpers.ToStringTest(counter);

		TestHelpers.assertContains(counter.toString(), "Str1", "Str4", "6");
	}

	@Test
	public void testToStringGeneric() {
		ConcurrentMappedCounter<Integer> counter = new ConcurrentMappedCounter<>();
		TestHelpers.ToStringTest(counter);

		counter.addInt(234, 3);
		counter.addInt(754, 6);

		TestHelpers.ToStringTest(counter);

		TestHelpers.assertContains(counter.toString(), "234", "754", "6");
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
            }
        });

        assertEquals(NUMBER_OF_THREADS*NUMBER_OF_TESTS, counter.get("sum"));
        assertEquals(NUMBER_OF_THREADS, counter.get("iter1"));

    	assertEquals(NUMBER_OF_THREADS*NUMBER_OF_TESTS * 2, counter.sum());
    }


	@Test
	public void testRemove() {
		MappedCounter<String> counter = new ConcurrentMappedCounter<>();
		assertEquals(0, counter.get("some"));
		assertNotNull(counter.keys());
		assertNotNull(counter.entries());

		counter.addInt("test", 1);
		assertEquals(1, counter.get("test"));

		counter.remove("test");
		assertEquals(0, counter.get("test"));
	}
}
