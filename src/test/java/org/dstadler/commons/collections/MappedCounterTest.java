package org.dstadler.commons.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.dstadler.commons.testing.TestHelpers;
import org.junit.Test;

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

        counter.remove("test");
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
    public void testWithComparable() {
        MappedCounter<TestEnum> counter = new MappedCounterImpl<>();

        counter.add(TestEnum.C, 1);
        counter.add(TestEnum.B, 1);
        counter.add(TestEnum.A, 1);

        // sorted by enum-order as Enum is a comparable
        assertEquals("{A=1, C=1, B=1}", counter.sortedMap().toString());
    }
}
