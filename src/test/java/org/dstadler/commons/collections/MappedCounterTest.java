package org.dstadler.commons.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.Test;

public class MappedCounterTest {
    protected <T> MappedCounter<T> createCounter() {
        return new MappedCounterImpl<>();
    }

    @Test
    public void test() {
        MappedCounter<String> counter = createCounter();
        assertEquals(0, counter.get("some"));
        assertNotNull(counter.keys());
        assertNotNull(counter.entries());

        counter.add("test", 1);
        assertEquals(1, counter.get("test"));

        counter.add("test", 2);
        assertEquals(3, counter.get("test"));

        counter.add("other", 14);
        counter.add("third", 5);
        assertEquals(3, counter.get("test"));
        assertEquals(14, counter.get("other"));
        assertEquals(5, counter.get("third"));

        Map<String, Long> sortedMap = counter.sortedMap();
        Iterator<Entry<String, Long>> iterator = sortedMap.entrySet().iterator();

        // sorted by decreasing value
        assertEquals(Long.valueOf(14), iterator.next().getValue());
        assertEquals(Long.valueOf(5), iterator.next().getValue());
        assertEquals(Long.valueOf(3), iterator.next().getValue());

        assertEquals(22, counter.sum());
    }

	@Test
	public void getNotFound() {
		MappedCounter<String> counter = createCounter();

		assertEquals(0, counter.get("not exist"));
		assertEquals(0, counter.get(null));
	}

	@Test
	public void removeNotFound() {
		MappedCounter<String> counter = createCounter();

		assertEquals(0, counter.remove("not exist"));
		assertEquals(0, counter.remove(null));
	}

    @Test
    public void testSortedMapSorted() {
        MappedCounter<String> counter = createCounter();

        counter.add("test", 1);
        assertEquals(1, counter.get("test"));
        counter.add("other", 1);
        counter.add("third", 1);
        counter.add("fourth", 1);
        counter.add("fifth", 1);
        assertEquals(1, counter.get("test"));
        assertEquals(1, counter.get("other"));
        assertEquals(1, counter.get("third"));
        assertEquals(1, counter.get("fourth"));
        assertEquals(1, counter.get("fifth"));

        assertEquals("{fifth=1, fourth=1, other=1, test=1, third=1}", counter.sortedMap().toString());
        assertEquals(5, counter.sum());

		counter.inc("other");
		assertEquals(2, counter.get("other"));
		assertEquals("{other=2, fifth=1, fourth=1, test=1, third=1}", counter.sortedMap().toString());
		assertEquals(6, counter.sum());

        counter.inc("new");
        assertEquals(1, counter.get("new"));
        assertEquals("{other=2, fifth=1, fourth=1, new=1, test=1, third=1}", counter.sortedMap().toString());
        assertEquals(7, counter.sum());

        counter.add(null, 15);
        assertEquals(15, counter.get(null));
        assertEquals("{null=15, other=2, fifth=1, fourth=1, new=1, test=1, third=1}", counter.sortedMap().toString());
        assertEquals(22, counter.sum());

        counter.add(null, -15);
        assertEquals(0, counter.get(null));
        assertEquals("{other=2, fifth=1, fourth=1, new=1, test=1, third=1, null=0}", counter.sortedMap().toString());
        assertEquals(7, counter.sum());

        counter.inc(null);
        assertEquals(1, counter.get(null));
        assertEquals("{other=2, null=1, fifth=1, fourth=1, new=1, test=1, third=1}", counter.sortedMap().toString());
        assertEquals(8, counter.sum());
    }

    @Test
    public void testGeneric() {
        MappedCounter<Integer> counter = createCounter();
        assertEquals(0, counter.get(24));
        assertNotNull(counter.keys());
        assertNotNull(counter.entries());

        counter.add(26, 1);
        assertEquals(1, counter.get(26));

        counter.add(26, 2);
        assertEquals(3, counter.get(26));

        counter.add(88, 14);
        counter.add(-3432, 5);
        assertEquals(3, counter.get(26));
        assertEquals(14, counter.get(88));
        assertEquals(5, counter.get(-3432));

        Map<Integer, Long> sortedMap = counter.sortedMap();
        Iterator<Entry<Integer, Long>> iterator = sortedMap.entrySet().iterator();

        // sorted by decreasing value
        assertEquals(Long.valueOf(14), iterator.next().getValue());
        assertEquals(Long.valueOf(5), iterator.next().getValue());
        assertEquals(Long.valueOf(3), iterator.next().getValue());
    }

    @Test
    public void testToString() {
        MappedCounter<String> counter = createCounter();
        TestHelpers.ToStringTest(counter);

        counter.add("Str1", 3);
        counter.add("Str4", 6);

        TestHelpers.ToStringTest(counter);

        TestHelpers.assertContains(counter.toString(), "Str1", "Str4", "6");
    }

    @Test
    public void testToStringGeneric() {
        MappedCounter<Integer> counter = createCounter();
        TestHelpers.ToStringTest(counter);

        counter.add(234, 3);
        counter.add(754, 6);

        TestHelpers.ToStringTest(counter);

        TestHelpers.assertContains(counter.toString(), "234", "754", "6");
    }

    @Test
    public void testRemove() {
        MappedCounter<String> counter = createCounter();
        assertEquals(0, counter.get("some"));
        assertNotNull(counter.keys());
        assertNotNull(counter.entries());

        counter.add("test", 1);
        assertEquals(1, counter.get("test"));

        assertEquals(1, counter.remove("test"));
        assertEquals(0, counter.get("test"));

        counter.add("test", 2);
        assertEquals(2, counter.get("test"));
        assertEquals(2, counter.remove("test"));
        assertEquals(0, counter.get("test"));
    }

    @Test
    public void testCount() {
        MappedCounter<String> counter = createCounter();
        assertEquals(0, counter.get("some"));

        counter.count(Collections.emptyList());
        assertEquals(0, counter.sortedMap().size());

        counter.count(Collections.singleton("some"));
        assertEquals(1, counter.sortedMap().size());
        assertEquals(1, counter.get("some"));

        counter.count(Collections.singleton("some"));
        assertEquals(1, counter.sortedMap().size());
        assertEquals(2, counter.get("some"));

        counter.count(Arrays.asList("some", "some2", "some2"));
        assertEquals(2, counter.sortedMap().size());
        assertEquals(3, counter.get("some"));
        assertEquals(2, counter.get("some2"));

        counter.clear();
        assertEquals(0, counter.get("some"));
        assertEquals("{}", counter.sortedMap().toString());
    }

    @Test
    public void testNullKey() {
        MappedCounter<String> counter = createCounter();

        counter.add(null, 1);

        assertEquals(1, counter.get(null));
        assertEquals("{null=1}", counter.sortedMap().toString());

        counter.add("some", 2);
        counter.add("more", 3);

        assertEquals(1, counter.get(null));
        assertEquals("{more=3, some=2, null=1}", counter.sortedMap().toString());

        // NPE with the same value
        counter.add("other", 1);

        assertEquals(1, counter.get(null));
        assertEquals("{more=3, some=2, null=1, other=1}", counter.sortedMap().toString());

        // NPE with the same value
        counter.add("third", 1);

        assertEquals(1, counter.get(null));
        assertEquals("{more=3, some=2, null=1, other=1, third=1}", counter.sortedMap().toString());
    }

    private enum TestEnum {
        A, C, B
    }

    @Test
    public void testSortedNonComparable() {
        MappedCounter<Integer> counter = createCounter();

        for (int i = 0; i < 1000; i++) {
            counter.add(i, 1);
        }

        Map<Integer, Long> map = counter.sortedMap();
        int i = 0;
        for (Entry<Integer, Long> entry : map.entrySet()) {
            assertEquals(Integer.valueOf(i), entry.getKey());
            assertEquals(Long.valueOf(1L), entry.getValue());

            i++;
        }
        assertEquals(1000, i);
    }

    @Test
    public void testSortedNonComparableValue() {
        MappedCounter<Integer> counter = createCounter();

        for (int i = 0; i < 1000; i++) {
            counter.add(i, i);
        }

        Map<Integer, Long> map = counter.sortedMap();
        int i = 1000;
        for (Entry<Integer, Long> entry : map.entrySet()) {
            i--;

            assertEquals(Integer.valueOf(i), entry.getKey());
            assertEquals(Long.valueOf(i), entry.getValue());
        }

        assertEquals(0, i);
    }

    @Test
    public void testWithComparable() {
        MappedCounter<TestEnum> counter = createCounter();

        counter.add(TestEnum.C, 1);
        counter.add(TestEnum.B, 1);
        counter.add(TestEnum.A, 1);

        // sorted by enum-order as Enum is a comparable
        assertEquals("{A=1, C=1, B=1}", counter.sortedMap().toString());
    }

    @Test
    public void testWithNonComparable() {
        MappedCounter<Object> counter = createCounter();

        counter.add(new Object(), 1);
        counter.add(new Object(), 1);
        counter.add(new Object(), 1);

        // sorted by enum-order as Enum is a comparable
        assertEquals("{java.lang.Object=1, java.lang.Object=1, java.lang.Object=1}",
				counter.sortedMap().toString().replaceAll("@[\\da-f]+", ""));
    }

	@Test
	public void testAddAll() {
		MappedCounter<Object> counter = createCounter();

		counter.add(new Object(), 1);
		counter.add(new Object(), 1);
		counter.add(new Object(), 1);

		MappedCounter<Object> added = createCounter();
		added.addAll(counter);

		assertEquals("{java.lang.Object=1, java.lang.Object=1, java.lang.Object=1}",
				counter.sortedMap().toString().replaceAll("@[\\da-f]+", ""));
		assertEquals("{java.lang.Object=1, java.lang.Object=1, java.lang.Object=1}",
				added.sortedMap().toString().replaceAll("@[\\da-f]+", ""));

		added.addAll(counter);

		assertEquals("{java.lang.Object=1, java.lang.Object=1, java.lang.Object=1}",
				counter.sortedMap().toString().replaceAll("@[\\da-f]+", ""));
		assertEquals("{java.lang.Object=2, java.lang.Object=2, java.lang.Object=2}",
				added.sortedMap().toString().replaceAll("@[\\da-f]+", ""));

		added.add("blabla", 5);
		added.addAll(counter);
		assertEquals("{blabla=5, java.lang.Object=3, java.lang.Object=3, java.lang.Object=3}",
				added.sortedMap().toString().replaceAll("@[\\da-f]+", ""));
	}

	@Test
	public void testMixedComparable() {
		MappedCounter<Object> counter = createCounter();

		counter.add(new Object(), 1);
		counter.add("blabla", 3);

		assertEquals("{blabla=3, java.lang.Object=1}", counter.sortedMap().toString().replaceAll("@[\\da-f]+", ""));
	}

	@Test
	public void testComparator() {
		MappedCounterImpl.CounterComparator<String> comparator = new MappedCounterImpl.CounterComparator<>();

		TestHelpers.ComparatorTest(comparator, getEntry(null, 1L), getEntry(null, 1L), getEntry("1", 1L), false);
		TestHelpers.ComparatorTest(comparator, getEntry(null, 1L), getEntry(null, 1L), getEntry(null, 2L), true);
		TestHelpers.ComparatorTest(comparator, getEntry("1", 1L), getEntry("1", 1L), getEntry("1", 2L), true);
		TestHelpers.ComparatorTest(comparator, getEntry("1", 1L), getEntry("1", 1L), getEntry(null, 2L), true);
		TestHelpers.ComparatorTest(comparator, getEntry("1", 1L), getEntry("1", 1L), getEntry("0", 1L), true);
		TestHelpers.ComparatorTest(comparator, getEntry("1", 1L), getEntry("1", 1L), getEntry("2", 1L), false);
		TestHelpers.ComparatorTest(comparator, getEntry("1", 1L), getEntry("1", 1L), getEntry("2", 1L), false);
	}

	@Test
	public void testComparatorObject() {
		MappedCounterImpl.CounterComparator<Object> comparator = new MappedCounterImpl.CounterComparator<>();

		// compare different types
		Object obj = "abc";
		Object equ = "abc";
		Object notEqu = new Object();
		TestHelpers.ComparatorTest(comparator, getEntry(obj, 1L), getEntry(equ, 1L), getEntry(notEqu, 1L), false);

		obj = new TestComparable();
		equ = new TestComparable();
		notEqu = "abc";
		TestHelpers.ComparatorTest(comparator, getEntry(obj, 1L), getEntry(equ, 1L), getEntry(notEqu, 1L), true);
	}

	private static <T> Entry<T, Long> getEntry(T key, long value) {
		Map<T, Long> map = new HashMap<>();
		map.put(key, value);
		return map.entrySet().iterator().next();
	}

	private static class TestComparable implements Comparable<Object> {

		@Override
		public int hashCode() {
			return 1;
		}

		@Override
		public int compareTo(Object o) {
			return Long.compare(hashCode(), o.hashCode());
		}
	}
}
