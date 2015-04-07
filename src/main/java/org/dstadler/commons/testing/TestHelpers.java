package org.dstadler.commons.testing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Copied from project test.util to use it in independent projects from here.
 *
 * @author dominik.stadler
 *
 */
public class TestHelpers {
	/**
	 * Verify that the provided object implements the basic requirements of an "equals()" method correctly, i.e. equal
	 * objects are equal, non-equal objects are not equal.
	 *
	 * Additionally some additional checks are performed, e.g.: - Reflexive: Objects should be equal to themselves -
	 * Symmetric: Equal objects should be equal in both directions - Transitive: Equal objects should have the same
	 * hashCode() - Handle null in equals gracefully - Handle foreign object types in equals gracefully
	 *
	 *
	 * @param obj
	 *            An object of the type to test, should override equals()
	 * @param equal
	 *            An object of the type, verified to be equal to "obj"
	 * @param notequal
	 *            An object of the type which should be different from "obj" and "equal"
	 */
	public static void EqualsTest(final Object obj, final Object equal, final Object notequal) {
		// none of the three should be null
		assertNotNull("Object in EqualsTest should not be null!", obj);
		assertNotNull("Equals-object in EqualsTest should not be null!", equal);
		assertNotNull("Non-equal-object in EqualsTest should not be null!", notequal);

		// make sure different objects are passed in
		assertFalse("Object and equals-object in EqualsTest should not be identical", obj == equal);	// NOPMD
		assertFalse("Object and non-equals-object in EqualsTest should not be identical", obj == notequal);	// NOPMD

		// make sure correct objects are passed
		assertTrue("Classes of objects in EqualsTest should be equal!", obj.getClass().equals(equal.getClass()));	// NOPMD
		assertTrue("Classes of objects in EqualsTest should be equal!", obj.getClass().equals(	// NOPMD
				notequal.getClass()));

		// make sure correct parameters are passed
		// equal should be equal to obj, not-equal should not be equal to obj!
		assertTrue("Object and equal-object should be equal in EqualsTest!", obj.equals(equal));	// NOPMD
		assertFalse("Object and non-equal-object should not be equal in EqualsTest!", obj.equals(notequal));	// NOPMD

		// first test some general things that should be true with equals

		// reflexive: equals to itself
		assertTrue("Reflexive: object should be equal to itself in EqualsTest!", obj.equals(obj));	// NOPMD
		assertTrue("Reflexive: equal-object should be equal to itself in EqualsTest!", equal.equals(equal));	// NOPMD
		assertTrue("Reflexive: non-equal-object should be equal to itself in EqualsTest!", notequal	// NOPMD
				.equals(notequal));

		// not equals to null
		assertFalse("Object should not be equal to null in EqualsTest!", obj.equals(null));	// NOPMD - null-equals() is intended here
		assertFalse("Equal-object should not be equal to null in EqualsTest!", equal.equals(null));	// NOPMD - null-equals() is intended here
		assertFalse("Non-equal-object should not be equal to null in EqualsTest!", notequal.equals(null));	// NOPMD - null-equals() is intended here

		// not equals to a different type of object
		assertFalse("Object should not be equal to an arbitrary string in EqualsTest!", obj
						.equals("TestString"));	// NOPMD

		// then test some things with another object that should be equal

		// symmetric, if one is (not) equal to another then the reverse must be true
		assertTrue("Symmetric: Object should be equal to equal-object in EqualsTest", obj.equals(equal));	// NOPMD
		assertTrue("Symmetric: Equals-object should be equal to object in EqualsTest!", equal.equals(obj));	// NOPMD
		assertFalse("Symmetric: Object should NOT be equal to non-equal-object in EqualsTest", obj	// NOPMD
				.equals(notequal));
		assertFalse("Symmetric: Non-equals-object should NOT be equal to object in EqualsTest!", notequal	// NOPMD
				.equals(obj));

		// transitive: if a.equals(b) and b.equals(c) then a.equals(c)
		// not tested right now

		// hashCode: equal objects should have equal hash code
		assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!",	// NOPMD
				obj.hashCode() == equal.hashCode());
		assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!", obj.hashCode() == obj	// NOPMD
				.hashCode());
		assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!",	// NOPMD
				equal.hashCode() == equal.hashCode());
		assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!",	// NOPMD
				notequal.hashCode() == notequal.hashCode());
	}

	/**
	 * Helper method to verify some basic assumptions about the compareTo() method.
	 *
	 * This can be used to verify basic assertions on an implementation of Comparable.
	 *
	 * @param obj
	 *            The object to use for compareTo()
	 * @param equal
	 *            An object which is equal to "obj", but not the same object!
	 * @param notequal
	 *            An object which is not equal to "obj"
	 */
	public static <T extends Comparable<T>> void CompareToTest(final T obj, final T equal,
			final T notequal, boolean notEqualIsLess) {
		// none of the three should be null
		assertNotNull("Object in CompareToTest should not be null!", obj);
		assertNotNull("Equals-object in CompareToTest should not be null!", equal);	// NOPMD
		assertNotNull("Non-equal-object in CompareToTest should not be null!", notequal);	// NOPMD

		// make sure different objects are passed in
		assertFalse("Object and equals-object in CompareToTest should not be identical", obj == equal);	// NOPMD
		assertFalse("Object and non-equals-object in CompareToTest should not be identical", obj == notequal);	// NOPMD

		// make sure correct parameters are passed
		// equal should be equal to obj, not-equal should not be equal to obj!
		assertEquals("Object and equal-object should compare in CompareToTest!", 0, obj.compareTo(equal));
		assertFalse("Object and non-equal-object should not compare in CompareToTest!", 0 == obj	// NOPMD
				.compareTo(notequal));
		assertFalse("Equal-object and non-equal-object should not compare in CompareToTest!", 0 == equal	// NOPMD
				.compareTo(notequal));

		// first test some general things that should be true with equals

		// reflexive: equals to itself
		assertEquals("Reflexive: object should be equal to itself in CompareToTest!", 0, obj.compareTo(obj));
		assertEquals("Reflexive: equal-object should be equal to itself in CompareToTest!", 0, equal
				.compareTo(equal));
		assertEquals("Reflexive: non-equal-object should be equal to itself in CompareToTest!", 0, notequal
				.compareTo(notequal));

		// not equals to null
		assertTrue("Object should not be equal to null in CompareToTest!",
				0 != obj.compareTo(null));
		assertTrue("Equal-object should not be equal to null in CompareToTest!",
				0 != equal.compareTo(null));
		assertTrue("Non-equal-object should not be equal to null in CompareToTest!",
				0 != notequal.compareTo(null));

		// not equals to a different type of object
		/*
		 * assertFalse("Object should not be equal to an arbitrary string in CompareToTest!" , 0 ==
		 * obj.compareTo("TestString"));
		 */

		// then test some things with another object that should be equal

		// symmetric, if one is (not) equal to another then the reverse must be true
		assertEquals("Symmetric: Object should be equal to equal-object in CompareToTest", 0, obj	// NOPMD
				.compareTo(equal));
		assertEquals("Symmetric: Equals-object should be equal to object in CompareToTest!", 0, equal	// NOPMD
				.compareTo(obj));
		assertFalse("Symmetric: Object should NOT be equal to non-equal-object in CompareToTest", 0 == obj	// NOPMD
				.compareTo(notequal));
		assertFalse("Symmetric: Non-equals-object should NOT be equal to object in CompareToTest!",		// NOPMD
				0 == notequal.compareTo(obj));
		assertEquals("Symnmetric: Comparing object and non-equal-object in both directions should lead to the same result.",
				signum(obj.compareTo(notequal)), (-1)*signum(notequal.compareTo(obj)));

		// transitive: if a.equals(b) and b.equals(c) then a.equals(c)
		// not tested right now

		assertEquals("Congruence: Comparing object and non-equal-object should have the same result as comparing the equal object and the non-equal-object",
				signum(obj.compareTo(notequal)), signum(equal.compareTo(notequal)));

		if(notEqualIsLess) {
			assertTrue("Item 'notequal' should be less than item 'equal' in CompareToTest, but compare was: " + obj.compareTo(notequal),
					obj.compareTo(notequal) > 0);
		} else {
			assertTrue("Item 'notequal' should be higher than item 'equal' in CompareToTest, but compare was: " + obj.compareTo(notequal),
					obj.compareTo(notequal) < 0);
		}

		// ensure equals() and hashCode() are implemented as well here
		assertTrue("Findbugs: Comparable objects should implement equals() the same way as compareTo().", obj.equals(equal));
        assertFalse("Findbugs: Comparable objects should implement equals() the same way as compareTo().", obj.equals(notequal));
        EqualsTest(obj, equal, notequal);
        assertEquals("Findbugs: Comparable objects should implement hashCode() the same way as compareTo().", obj.hashCode(), equal.hashCode());
        HashCodeTest(obj, equal);
	}

	/**
	 * Helper method to verify some basic assumptions about implementations of the Comparator interface.
	 *
	 * This can be used.
	 *
	 * @param comparator
	 *            The implementation of the Comparator.
	 * @param obj
	 *            The object to use.
	 * @param equal
	 *            An object which is equal to "obj", but not the same object!
	 * @param notequal
	 *            An object which is not equal to "obj"
	 */
	public static <T> void ComparatorTest(final Comparator<T> comparator, final T obj, final T equal,
			final T notequal, boolean notEqualIsLess) {
		// none of the three should be null
		assertNotNull("Object in ComparatorTest should not be null!", obj);
		assertNotNull("Equals-object in ComparatorTest should not be null!", equal);	// NOPMD
		assertNotNull("Non-equal-object in ComparatorTest should not be null!", notequal);	// NOPMD

		// make sure different objects are passed in
		assertFalse("Object and equals-object in ComparatorTest should not be identical", obj == equal);	// NOPMD
		assertFalse("Object and non-equals-object in ComparatorTest should not be identical", obj == notequal);	// NOPMD

		// make sure correct parameters are passed
		// equal should be equal to obj, not-equal should not be equal to obj!
		assertEquals("Object and equal-object should compare in ComparatorTest!", 0, comparator.compare(obj, equal));
		assertFalse("Object and non-equal-object should not compare in ComparatorTest!", 0 == comparator.compare(obj	// NOPMD
				, notequal));

		// first test some general things that should be true with equals

		// reflexive: equals to itself
		assertEquals("Reflexive: object should be equal to itself in ComparatorTest!", 0, comparator.compare(obj, obj));
		assertEquals("Reflexive: equal-object should be equal to itself in ComparatorTest!", 0, comparator.compare(equal
				, equal));
		assertEquals("Reflexive: non-equal-object should be equal to itself in ComparatorTest!", 0, comparator.compare(notequal
				, notequal));

		// not equals to null, not checked currently as most Comparators expect non-null input at all times
		/*assertTrue("Object should not be equal to null in ComparatorTest!",
				0 != comparator.compare(obj, null));
		assertTrue("Equal-object should not be equal to null in ComparatorTest!",
				0 != comparator.compare(equal, null));
		assertTrue("Non-equal-object should not be equal to null in ComparatorTest!",
				0 != comparator.compare(notequal, null));*/

		// not equals to a different type of object
		/*
		 * assertFalse("Object should not be equal to an arbitrary string in ComparatorTest!" , 0 ==
		 * obj, "TestString"));
		 */

		// then test some things with another object that should be equal

		// symmetric, if one is (not) equal to another then the reverse must be true
		assertEquals("Symmetric: Object should be equal to equal-object in ComparatorTest", 0, comparator.compare(obj	// NOPMD
				, equal));
		assertEquals("Symmetric: Equals-object should be equal to object in ComparatorTest!", 0, comparator.compare(equal	// NOPMD
				, obj));
		assertFalse("Symmetric: Object should NOT be equal to non-equal-object in ComparatorTest", 0 == comparator.compare(obj	// NOPMD
				, notequal));
		assertFalse("Symmetric: Non-equals-object should NOT be equal to object in ComparatorTest!",	// NOPMD
				0 == comparator.compare(notequal, obj));
		assertEquals("Symnmetric: Comparing object and non-equal-object in both directions should lead to the same result.",
				signum(comparator.compare(obj, notequal)), (-1)*signum(comparator.compare(notequal, obj)));

		// transitive: if a.equals(b) and b.equals(c) then a.equals(c)
		// not tested right now

		assertEquals("Congruence: Comparing object and non-equal-object should have the same result as comparing the equal object and the non-equal-object",
				signum(comparator.compare(obj, notequal)), signum(comparator.compare(equal, notequal)));

		if(notEqualIsLess) {
			assertTrue("Item 'notequal' should be less than item 'equal' in ComparatorTest, but compare was: " + comparator.compare(notequal, obj),
					comparator.compare(notequal, obj) < 0);
		} else {
			assertTrue("Item 'notequal' should be higher than item 'equal' in ComparatorTest, but compare was: " + comparator.compare(notequal, obj),
					comparator.compare(notequal, obj) > 0);
		}

		// additionally test with null
		assertEquals("compare(null,null) should have 0 as compare-result", 0, comparator.compare(null, null));
		assertTrue("compare(obj,null) should not have 0 as compare-result", comparator.compare(obj, null) != 0);
		assertTrue("compare(null,obj) should not have 0 as compare-result", comparator.compare(null, obj) != 0);
	}

	private static int signum(int i) {
		if(i < 0) {
			return -1;
		} else if (i > 0) {
			return 1;
		}

		return 0;
	}

	/**
	 * Run some general tests on the toString method. This static method is used in tests for classes that overwrite
	 * toString().
	 *
	 * @param obj
	 *            The object to test toString(). This should be an object of a type that overwrites toString()
	 *
	 */
	public static void ToStringTest(final Object obj) {
		// toString should not return null
		assertNotNull("A derived toString() should not return null!", obj.toString());

		// toString should not return an empty string
		assertFalse("A derived toString() should not return an empty string!", obj.toString().equals(""));

		// check that calling it multiple times leads to the same value
		String value = obj.toString();
		for (int i = 0; i < 10; i++) {
			assertEquals("toString() is expected to result in the same result across repeated calls!", value,
					obj.toString());
		}
	}

	/**
	 * Run some generic tests on the derived clone-method.
	 *
	 * We need to do this via reflection as the clone()-method in Object is protected and the Cloneable interface does
	 * not include a public "clone()".
	 *
	 * @param obj
	 *            The object to test clone for.
	 */
	public static void CloneTest(final Cloneable obj) throws Exception {
		final Method m = obj.getClass().getMethod("clone", new Class[] {});
		assertNotNull("Need to find a method called 'clone' in object of type '" + obj.getClass().getName()
				+ "' in CloneTest!", m);
		// assertTrue("Method 'clone' on object of type '" +
		// obj.getClass().getName() + "' needs to be accessible in
		// CloneTest!",
		// m.isAccessible());

		// clone should return a different object, not the same again
		assertTrue("clone() should not return the object itself in CloneTest!", obj != m.invoke(obj,	// NOPMD
				new Object[] {}));

		// should return the same type of object
		assertTrue("clone() should return the same type of object (i.e. the same class) in CloneTest!", m	// NOPMD
				.invoke(obj, new Object[] {}).getClass() == obj.getClass());

		// cloned objects should be equal to the original object
		assertTrue("clone() should return an object that is equal() to the original object in CloneTest!", m
				.invoke(obj, new Object[] {}).equals(obj));
	}

	/**
	 * Checks certain assumption that are made for the hashCode() method
	 *
	 * @param obj
	 *            An Object that override the hasCode() method.
	 *
	 * @throws Exception
	 */
	public static void HashCodeTest(final Object obj, final Object equ) {
		assertFalse(	// NOPMD
						"HashCodeTest expects two distinct objects with equal hashCode, but the same object is provided twice!",
						obj == equ);

		// The same object returns the same hashCode always
		final int hash = obj.hashCode();
		assertEquals("hashCode() on object returned different hash after some iterations!", hash, obj
						.hashCode());
		assertEquals("hashCode() on object returned different hash after some iterations!", hash, obj
						.hashCode());
		assertEquals("hashCode() on object returned different hash after some iterations!", hash, obj
						.hashCode());
		assertEquals("hashCode() on object returned different hash after some iterations!", hash, obj
						.hashCode());
		assertEquals("hashCode() on object returned different hash after some iterations!", hash, obj
						.hashCode());

		// equal objects must have the same hashCode
		// the other way around is not required,
		// different objects can have the same hashCode!!
		assertEquals(
						"Equal Assert failed, but input to HashCodeTest should be two equal objects! Check if the class implements equals() as well to fullfill this contract",
						obj, equ);
		assertEquals("Equal objects should have equal hashCode() by Java contract!", obj.hashCode(), equ
				.hashCode());
	}

	/**
	 * Verifies certain assumptions on an Enum class.
	 *
	 * @param <T>
	 * @param enumtype
	 * @param enumclass
	 * @param element
	 *
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static <T extends Enum<T>> void EnumTest(Enum<T> enumtype, Class<T> enumclass, String element)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// check valueOf()
		assertEquals(enumtype, Enum.valueOf(enumclass, element));

		// check values()
		Method m = enumclass.getMethod("values", (Class[]) null);
		Object obj = m.invoke(enumtype, (Object[]) null);
		assertNotNull(obj);
		assertTrue(obj instanceof Object[]);

		// check existing valeOf()
		obj = Enum.valueOf(enumclass, element);
		assertNotNull(obj);
		// Findbugs: useless check: assertTrue(obj instanceof Enum<?>);

		// check non-existing valueOf
		try {
			Enum.valueOf(enumclass, "nonexistingenumelement");
			fail("Should catch exception IllegalArgumentException when calling Enum.valueOf() with incorrect enum-value!");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("No enum const class"));
		}
	}

	/**
	 * Small helper to verify that a Throwable contains the specified sub-string as part of it's message.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param str
	 * @param search
	 */
	public static void assertContains(final Throwable throwable, final String... searches) {
		assertNotNull("Cannot verify message contents of a Throwable when it is null.", throwable);
        assertTrue("Specify at least one search-term to be searched in the string", searches.length > 0);

		String str = throwable.getMessage();
		if(str == null) {
			throw new IllegalArgumentException("Throwable of type " + throwable.getClass().toString() + " contains a null-string as message, cannot assertContains", throwable);
		}
		for(String search : searches) {
			assertTrue("Expected to find string '" + search + "', but was not contained in provided string '" + str + "'\n" + ExceptionUtils.getStackTrace(throwable), str.contains(search));
		}
	}

	/**
	 * Small helper to verify that a Throwable contains the specified sub-string as part of it's message.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param str
	 * @param search
	 */
	public static void assertContains(final String msg, final Throwable throwable, final String... searches) {
		assertNotNull("Cannot verify message contents of a Throwable when it is null.", throwable);
        assertTrue("Specify at least one search-term to be searched in the string", searches.length > 0);

		String str = throwable.getMessage();
		if(str == null) {
			throw new IllegalArgumentException("Throwable of type " + throwable.getClass().toString() + " contains a null-string as message, cannot assertContains", throwable);
		}
		for(String search : searches) {
			assertTrue(msg + ". Expected to find string '" + search + "', but was not contained in provided string '" + str + "'\n" + ExceptionUtils.getStackTrace(throwable), str.contains(search));
		}
	}

	/**
	 * Small helper to verify that a string contains a sub-string.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param str
	 * @param search
	 */
	public static void assertContains(final String str, final String... searches) {
		assertNotNull("Cannot assertContains on a null-string", str);
		assertTrue("Specify at least one search-term to be searched in the string", searches.length > 0);

		for(String search : searches) {
			assertTrue("Expected to find string '" + search + "', but was not contained in provided string '" + str + "'", str.contains(search));
		}
	}

	/**
	 * Small helper to verify that a string contains a sub-string.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param str
	 * @param search
	 */
	public static void assertContainsMsg(final String msg, final String str, final String... searches) {
		assertNotNull("Cannot assertContains on a null-string", str);
        assertTrue("Specify at least one search-term to be searched in the string", searches.length > 0);

		for(String search : searches) {
			assertTrue(msg + ". Expected to find string '" + search + "', but was not contained in provided string '" + str + "'", str.contains(search));
		}
	}

	/**
	 * Small helper to verify that a string does not contain a sub-string.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param str
	 * @param search
	 */
	public static void assertNotContains(final String str, final String... searches) {
		assertNotNull("Cannot assertNotContains on a null-string", str);
        assertTrue("Specify at least one search-term to be searched in the string", searches.length > 0);

		for(String search : searches) {
			assertFalse("Expected to NOT find '" + search + "' but was contained in string '" + str + "'", str.contains(search));
		}
	}


	/**
	 * Small helper to verify that a string does not contain a sub-string.
	 *
	 * This will provide a useful error message in case of failure.
	 *
	 * @param str
	 * @param search
	 */
	public static void assertNotContainsMsg(final String msg, final String str, final String... searches) {
		assertNotNull("Cannot assertNotContains on a null-string", str);
        assertTrue("Specify at least one search-term to be searched in the string", searches.length > 0);

		for(String search : searches) {
			assertFalse(msg + ". Expected to NOT find '" + search + "' but was contained in string '" + str + "'", str.contains(search));
		}
	}

	/**
	 * Allows to run code with different loglevel to cover cases where logging is only done with debug-log-level.
	 *
	 * Ensures that the log-level is reset back to the original value after the test is run, irrespective if it failed or not.
	 *
	 *  @param test A Runnable that executes the test-code
	 *  @param className The name that is used for the logger that should be adjusted
	 *  @param level The actual log-level that should be used, e.g. Level.FINE
	 */
	public static void runTestWithDifferentLogLevel(final Runnable test, final String className, final Level... levels) {
		Logger localLogger = Logger.getLogger(className);
		Level origLevel = localLogger.getLevel();
		for(Level level : levels) {
			localLogger.setLevel(level);
			try {
				test.run();
			} finally {
				localLogger.setLevel(origLevel);
			}
		}
	}

	/**
	 * Verify that the given URL is actually existing and results in a HTTP 200 return code
	 * if requested.
	 *
	 * This is used to stop tests early if required internet access is not available.
	 *
	 * @param urlString
	 * @param timeout
	 * @throws IOException
	 * @author dominik.stadler
	 */
	public static void assertURLWorks(String urlString, int timeout) throws IOException {
		URL url = new URL(urlString);

        HttpURLConnection conn = null;
        conn = (HttpURLConnection) url.openConnection();
        try {
	        conn.setDoOutput(false);
	        conn.setDoInput(true);
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);

	        /* if connecting is not possible this will throw a connection refused exception */
	        conn.connect();

	        assertEquals("Expect URL '" + urlString + "' to be available and return HTTP 200",
	        		HttpURLConnection.HTTP_OK, conn.getResponseCode());
        } finally {
        	conn.disconnect();
        }
	}
}
