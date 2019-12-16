package org.dstadler.commons.collections;

public class UnsupportedListTest extends AbstractUnsupportedCollectionTest<UnsupportedList<Object>> {
    @Override
    protected UnsupportedList<Object> instance() {
        return new UnsupportedList<Object>() {};
    }
}