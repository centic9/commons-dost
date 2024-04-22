package org.dstadler.commons.collections;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * A simple {@link java.util.List} implementation which throws {@link UnsupportedOperationException}
 * for every method.
 *
 * This can be used to implement implementations which only provide a
 * subset of the functionality of List
 */
public abstract class UnsupportedList<E> extends UnsupportedCollection<E> implements List<E> {
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public E get(int index) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

	// this is added in JDK 21, so not @Override here
	@SuppressWarnings("UnusedReturnValue")
	public List<E> reversed() {
		throw new UnsupportedOperationException("This operation is not supported");
	}

	// this is added in JDK 21, so not @Override here
	public E getFirst() {
		throw new UnsupportedOperationException("This operation is not supported");
	}

	// this is added in JDK 21, so not @Override here
	public E getLast() {
		throw new UnsupportedOperationException("This operation is not supported");
	}
}
