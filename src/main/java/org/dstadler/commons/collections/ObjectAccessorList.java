package org.dstadler.commons.collections;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link java.util.List} which wraps another list and provides a read-only view
 * of a specific property of the type of object contained in the original list.
 *
 * Any method which would modify the underlying list throws an {@link UnsupportedOperationException}.
 *
 * The implementation uses an accessor function to extract a property from the underlying list, e.g. it can
 * be constructed as follows:
 *
 * List&lt;String> list = new ObjectAccessorList&lt;>(originalList, MyObject::getStringProperty());
 *
 * Some reading methods also throw UnsupportedOperationException, mostly they cannot be implemented
 * without an "inverse" operation from R to E.
 *
 * Currently at least get(), size(), isEmpty(), iterator() and forEach() can be expected to work.
 *
 * Changes to the underlying list should usually have the expected effect on this implementation.
 *
 * Reading fom the the underlying list in multiple threads should work, modifying the underlying
 * list in multiple threads concurrently is not supported.
 *
 * @param <E> The type of objects stored in the underlying list
 * @param <R> The type of object this list provides by reading it from the
 *           underlying list via the provided accessor function
 */
public class ObjectAccessorList<R, E> extends UnsupportedList<R> {
    private final List<E> original;
    private final Function<E, R> accessor;

    public ObjectAccessorList(List<E> original, Function<E, R> accessor) {
        this.original = original;
        this.accessor = accessor;
    }

    @Override
    public R get(int index) {
        return accessor.apply(original.get(index));
    }

    @Override
    public int size() {
        return original.size();
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty();
    }

    @Override
    public Iterator<R> iterator() {
        return new Iterator<R>() {
            private final Iterator<E> it = original.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public R next() {
                return accessor.apply(it.next());
            }
        };
    }

    @Override
    public void forEach(Consumer<? super R> action) {
        original.forEach(e -> action.accept(accessor.apply(e)));
    }
}
