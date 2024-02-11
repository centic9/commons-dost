package org.dstadler.commons.collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class UnsupportedCollectionTest extends AbstractUnsupportedCollectionTest<UnsupportedCollection<Object>> {
    @Override
    protected UnsupportedCollection<Object> instance() {
        return new UnsupportedCollection<>() {};
    }

	@Test
	public void test() {
		UnsupportedCollection<Object> coll = instance();

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
	}
}