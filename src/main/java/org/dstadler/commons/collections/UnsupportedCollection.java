package org.dstadler.commons.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A simple abstract {@link java.util.Collection} implementation which throws {@link UnsupportedOperationException}
 * for every method.
 *
 * This can be used to implement implementations which only provide a
 * subset of the functionality of List
 */
public abstract class UnsupportedCollection<E> implements Collection<E> {
    @Override
    public int size() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    // Only overrides on Java 11: @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public Spliterator<E> spliterator() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public Stream<E> stream() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public Stream<E> parallelStream() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public void  forEach(Consumer<? super E> action) {
        throw new UnsupportedOperationException("This operation is not supported");
    }
}
