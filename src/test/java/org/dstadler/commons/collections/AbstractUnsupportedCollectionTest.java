package org.dstadler.commons.collections;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class AbstractUnsupportedCollectionTest<T> {
    // some methods are not overwritten on purpose
    protected static final Set<String> IGNORED_METHODS = new HashSet<>();
    static {
        IGNORED_METHODS.add("getClass");
        IGNORED_METHODS.add("hashCode");
        IGNORED_METHODS.add("equals");
        // clone
        IGNORED_METHODS.add("toString");
        IGNORED_METHODS.add("notify");
        IGNORED_METHODS.add("notifyAll");
        IGNORED_METHODS.add("wait");
        // finalize
    }

    protected abstract T instance();

    @Test
    public void allMethodsThrowException() throws IllegalAccessException {
        T collection = instance();

        // simply use reflection to verify that any that we can call throws an Exception
        for (Method method : collection.getClass().getMethods()) {
            if(IGNORED_METHODS.contains(method.getName())) {
                continue;
            }

            try {
                if (method.getParameterCount() == 0) {
                    method.invoke(collection);
                } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Object.class) {
                    method.invoke(collection, new Object());
                } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Object[].class) {
                    method.invoke(collection, new Object[]{new Object[] {}});
                } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Collection.class) {
                    method.invoke(collection, Collections.emptyList());
                } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Comparator.class) {
                    method.invoke(collection, Comparator.naturalOrder());
                } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Consumer.class) {
                    method.invoke(collection, (Consumer<Object>) o -> {});
                } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Predicate.class) {
                    method.invoke(collection, (Predicate<Object>) o -> false);
                } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == IntFunction.class) {
                    method.invoke(collection, (IntFunction<Object>) o -> 0);
                } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == UnaryOperator.class) {
                    method.invoke(collection, (UnaryOperator<Object>) o -> 0);
                } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == long.class) {
                    method.invoke(collection, 0L);
                } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == int.class) {
                    method.invoke(collection, 0);
                } else if (method.getParameterCount() == 2 && method.getParameterTypes()[0] == int.class &&
                        method.getParameterTypes()[1] == Object.class) {
                    method.invoke(collection, 0, new Object());
                } else if (method.getParameterCount() == 2 && method.getParameterTypes()[0] == int.class &&
                        method.getParameterTypes()[1] == Collection.class) {
                    method.invoke(collection, 0, Collections.emptyList());
                } else if (method.getParameterCount() == 2 && method.getParameterTypes()[0] == int.class &&
                        method.getParameterTypes()[1] == int.class) {
                    method.invoke(collection, 0, 0);
                } else {
                    fail("Don't know how to call method " + method);
                }
                fail("Method " + method + " did not throw an exception, but should");
            } catch (InvocationTargetException e) {
                // expected here
                assertTrue("Had: " + e.getCause(), e.getCause() instanceof UnsupportedOperationException);
            }
        }
    }
}