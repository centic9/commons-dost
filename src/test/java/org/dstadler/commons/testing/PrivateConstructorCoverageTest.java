package org.dstadler.commons.testing;

import static org.junit.Assert.fail;

import org.junit.Test;


/**
 *
 * @author dominik.stadler
 */
public class PrivateConstructorCoverageTest {

	/**
	 * Test method for {@link org.dstadler.commons.testing.PrivateConstructorCoverage#executePrivateConstructor(java.lang.Class)}.
	 * @throws Exception
	 */
	@Test
	public void testExecutePrivateConstructor() throws Exception {
		// run this on itself to cover it!
		PrivateConstructorCoverage.executePrivateConstructor(PrivateConstructorCoverage.class);

		// run it with an abstract class to check for exception
		try {
			PrivateConstructorCoverage.executePrivateConstructor(MyAbstract.class);
			fail("Should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Cannot run the private constructor for abstract classes");
		}
	}

	private abstract class MyAbstract {

	}
}
