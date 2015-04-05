package org.dstadler.commons.testing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * @author dominik.stadler
 *
 */
public class PrivateConstructorCoverage {
	/**
	 * Helper method for removing coverage-reports for classes with only static
	 * methods
	 * <p/>
	 * see for related EMMA ticket
	 * http://sourceforge.net/tracker/index.php?func=
	 * detail&aid=1173251&group_id=108932&atid=651900
	 *
	 * add this to the test case for any class that has only static methods
	 * where coverage reports the default constructor as not covered
	 *
	 * Template:
	 *
	 * <code>

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(<yourclass>.class);
	}

	 </code>
	 *
	 * @param targetClass
	 */
	public static <T> T executePrivateConstructor(final Class<T> targetClass) throws Exception {
        if(Modifier.isAbstract(targetClass.getModifiers())) {
        	throw new IllegalArgumentException("Cannot run the private constructor for abstract classes.");
        }

		// get the default constructor
		final Constructor<T> c = targetClass.getDeclaredConstructor(new Class[] {});

		// make it callable from the outside
		c.setAccessible(true);

		// call it
		return c.newInstance((Object[]) null);
	}
}
