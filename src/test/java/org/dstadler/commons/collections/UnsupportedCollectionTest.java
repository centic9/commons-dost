package org.dstadler.commons.collections;

public class UnsupportedCollectionTest extends AbstractUnsupportedCollectionTest<UnsupportedCollection<Object>> {
    @Override
    protected UnsupportedCollection<Object> instance() {
        return new UnsupportedCollection<Object>() {};
    }
}