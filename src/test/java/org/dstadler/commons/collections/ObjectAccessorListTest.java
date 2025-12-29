package org.dstadler.commons.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
        IGNORED_METHODS.add("getFirst");
        IGNORED_METHODS.add("getLast");
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
        assertEquals((Integer)obj1.hashCode(), list.getFirst());
        assertEquals((Integer)obj2.hashCode(), list.get(1));
        assertEquals((Integer)obj3.hashCode(), list.get(2));
        assertEquals(3, list.size());
        assertFalse(list.isEmpty());

		assertThrows(IndexOutOfBoundsException.class,
				() -> list.get(2363));

		//noinspection CastCanBeRemovedNarrowingVariableType
		assertEquals((Integer)obj1.hashCode(), ((UnsupportedList<Integer>)list).getFirst());
		//noinspection CastCanBeRemovedNarrowingVariableType
		assertEquals((Integer)obj3.hashCode(), ((UnsupportedList<Integer>)list).getLast());

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
				() -> list.removeFirst());
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

        assertEquals((Integer)obj1.hashCode(), list.getFirst());
        assertEquals((Integer)obj3.hashCode(), list.get(1));
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());

		assertThrows(IndexOutOfBoundsException.class,
				() -> list.get(2363));

		//noinspection CastCanBeRemovedNarrowingVariableType
		assertEquals((Integer)obj1.hashCode(), ((UnsupportedList<Integer>)list).getFirst());
		//noinspection CastCanBeRemovedNarrowingVariableType
		assertEquals((Integer)obj3.hashCode(), ((UnsupportedList<Integer>)list).getLast());

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
				() -> list.removeFirst());

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

	@Test
	public void testEmpty() {
		List<Integer> list = new ObjectAccessorList<>(Collections.emptyList(), Object::hashCode) {};
		assertTrue(list.isEmpty());
		//noinspection ConstantValue
		assertEquals(0, list.size());
        //noinspection SequencedCollectionMethodCanBeUsed
        assertThrows(IndexOutOfBoundsException.class,
                () -> list.get(0));
		assertThrows(NoSuchElementException.class,
                list::getFirst);
		assertThrows(NoSuchElementException.class,
                list::getLast);
		assertThrows(IndexOutOfBoundsException.class,
				() -> list.get(2363));
		//noinspection CastCanBeRemovedNarrowingVariableType
		assertThrows(NoSuchElementException.class,
				() -> ((UnsupportedList<Integer>)list).getFirst());
		//noinspection CastCanBeRemovedNarrowingVariableType
		assertThrows(NoSuchElementException.class,
				() -> ((UnsupportedList<Integer>)list).getLast());
	}
}