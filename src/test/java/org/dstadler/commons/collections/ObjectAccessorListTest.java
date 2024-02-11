package org.dstadler.commons.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ObjectAccessorListTest  extends AbstractUnsupportedCollectionTest<ObjectAccessorList<Integer, Object>> {
    @BeforeAll
    public static void setUpClass() {
        // list all methods which are actually implemented to not fail the checks in the parent class
        IGNORED_METHODS.add("get");
        IGNORED_METHODS.add("size");
        IGNORED_METHODS.add("isEmpty");
        IGNORED_METHODS.add("iterator");
        IGNORED_METHODS.add("forEach");
    }

    @Override
    protected ObjectAccessorList<Integer, Object> instance() {
        ArrayList<Object> objects = new ArrayList<>();
        objects.add(new Object());
        return new ObjectAccessorList<>(objects, Object::hashCode) {};
    }

    @Test
    public void testObjectAccess() {
        ArrayList<Object> objects = new ArrayList<>();
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        objects.add(obj1);
        objects.add(obj2);
        objects.add(obj3);

        List<Integer> list = new ObjectAccessorList<>(objects, Object::hashCode) {};
        assertEquals((Integer)obj1.hashCode(), list.get(0));
        assertEquals((Integer)obj2.hashCode(), list.get(1));
        assertEquals((Integer)obj3.hashCode(), list.get(2));
        assertEquals(3, list.size());
        assertFalse(list.isEmpty());

        Iterator<Integer> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals((Integer)obj1.hashCode(), it.next());
        assertTrue(it.hasNext());
        assertEquals((Integer)obj2.hashCode(), it.next());
        assertTrue(it.hasNext());
        assertEquals((Integer)obj3.hashCode(), it.next());
        assertFalse(it.hasNext());

        AtomicLong sum = new AtomicLong();
        list.forEach(sum::addAndGet);
        assertEquals(((long)obj1.hashCode())+obj2.hashCode()+obj3.hashCode(), sum.get());

		assertThrows(UnsupportedOperationException.class,
				() -> list.remove(0));
    }

    @Test
    public void testObjectAccessWithChanges() {
        ArrayList<Object> objects = new ArrayList<>();
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        objects.add(obj1);
        objects.add(obj2);
        objects.add(obj3);

        List<Integer> list = new ObjectAccessorList<>(objects, Object::hashCode) {};

        // modify the list
        objects.remove(obj2);

        assertEquals((Integer)obj1.hashCode(), list.get(0));
        assertEquals((Integer)obj3.hashCode(), list.get(1));
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());

        Iterator<Integer> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals((Integer)obj1.hashCode(), it.next());
        assertTrue(it.hasNext());
        assertEquals((Integer)obj3.hashCode(), it.next());
        assertFalse(it.hasNext());

        AtomicLong sum = new AtomicLong();
        list.forEach(sum::addAndGet);
        assertEquals(((long)obj1.hashCode())+obj3.hashCode(), sum.get());

		assertThrows(UnsupportedOperationException.class,
				() -> list.remove(0));

        // remove all objects
        objects.remove(obj1);
        objects.remove(obj3);

        assertEquals(0, list.size());

        assertFalse(list.iterator().hasNext());
        // also forEach does not invoke anything anymore
        list.forEach(integer -> {
            throw new IllegalStateException("Had: " + integer);
        });

        assertTrue(list.isEmpty());
    }
}