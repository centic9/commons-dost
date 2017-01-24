package org.dstadler.commons.collections;

import org.dstadler.commons.testing.TestHelpers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MappedCounterTest {
    @Test
    public void test() {
        MappedCounter<String> counter = new MappedCounterImpl<>();
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
    public void testSortedMapSorted() {
        MappedCounter<String> counter = new MappedCounterImpl<>();

        counter.addInt("test", 1);
        assertEquals(1, counter.get("test"));
        counter.addInt("other", 1);
        counter.addInt("third", 1);
        counter.addInt("fourth", 1);
        counter.addInt("fifth", 1);
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
        MappedCounter<Integer> counter = new MappedCounterImpl<>();
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
        MappedCounter<String> counter = new MappedCounterImpl<>();
        TestHelpers.ToStringTest(counter);

        counter.addInt("Str1", 3);
        counter.addInt("Str4", 6);

        TestHelpers.ToStringTest(counter);

        TestHelpers.assertContains(counter.toString(), "Str1", "Str4", "6");
    }

    @Test
    public void testToStringGeneric() {
        MappedCounter<Integer> counter = new MappedCounterImpl<>();
        TestHelpers.ToStringTest(counter);

        counter.addInt(234, 3);
        counter.addInt(754, 6);

        TestHelpers.ToStringTest(counter);

        TestHelpers.assertContains(counter.toString(), "234", "754", "6");
    }

    @Test
    public void testRemove() {
        MappedCounter<String> counter = new MappedCounterImpl<>();
        assertEquals(0, counter.get("some"));
        assertNotNull(counter.keys());
        assertNotNull(counter.entries());

        counter.addInt("test", 1);
        assertEquals(1, counter.get("test"));

        counter.remove("test");
        assertEquals(0, counter.get("test"));
    }

    @Test
    public void testCount() {
        MappedCounter<String> counter = new MappedCounterImpl<>();
        assertEquals(0, counter.get("some"));

        counter.count(Collections.<String>emptyList());
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
    }

    @Test
    public void testNullKey() {
        MappedCounter<String> counter = new MappedCounterImpl<>();

        counter.addInt(null, 1);

        assertEquals(1, counter.get(null));
        assertEquals("{null=1}", counter.sortedMap().toString());

        counter.addInt("some", 2);
        counter.addInt("more", 3);

        assertEquals(1, counter.get(null));
        assertEquals("{more=3, some=2, null=1}", counter.sortedMap().toString());

        // NPE with the same value
        counter.addInt("other", 1);

        assertEquals(1, counter.get(null));
        assertEquals("{more=3, some=2, null=1, other=1}", counter.sortedMap().toString());

        // NPE with the same value
        counter.addInt("third", 1);

        assertEquals(1, counter.get(null));
        assertEquals("{more=3, some=2, null=1, other=1, third=1}", counter.sortedMap().toString());
    }
}
