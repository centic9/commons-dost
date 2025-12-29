package org.dstadler.commons.collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class UnsupportedListTest extends AbstractUnsupportedCollectionTest<UnsupportedList<Object>> {
    @Override
    protected UnsupportedList<Object> instance() {
        return new UnsupportedList<>() {};
    }

	@Test
	public void test() {
		UnsupportedList<Object> coll = instance();

		assertThrows(UnsupportedOperationException.class,
				coll::size);
		assertThrows(UnsupportedOperationException.class,
				coll::isEmpty);
		assertThrows(UnsupportedOperationException.class,
				coll::iterator);
		assertThrows(UnsupportedOperationException.class,
				() -> coll.forEach(null));
		assertThrows(UnsupportedOperationException.class,
				() -> coll.add(null));
		assertThrows(UnsupportedOperationException.class,
                coll::getFirst);
		assertThrows(UnsupportedOperationException.class,
				coll::reversed);
		assertThrows(UnsupportedOperationException.class,
				coll::getFirst);
		assertThrows(UnsupportedOperationException.class,
				coll::getLast);
	}
}