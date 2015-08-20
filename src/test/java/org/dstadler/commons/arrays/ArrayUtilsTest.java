package org.dstadler.commons.arrays;

import static org.junit.Assert.assertEquals;

import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.junit.Test;

public class ArrayUtilsTest {

	@Test
	public void testToStringObjectArrayString() {
		assertEquals("null", ArrayUtils.toString(null, ", "));
		assertEquals("[]", ArrayUtils.toString(new Object[]{}, ", "));
		assertEquals("[str1]", ArrayUtils.toString(new Object[]{"str1"}, ", "));
		assertEquals("[str1, str2]", ArrayUtils.toString(new Object[]{"str1", "str2"}, ", "));
		assertEquals("[str1/str2]", ArrayUtils.toString(new Object[]{"str1", "str2"}, "/"));
	}

	@Test
	public void testToStringObjectArrayStringStringString() {
		assertEquals("null", ArrayUtils.toString(null, ", ", "asdfas", "asdfw3"));
		assertEquals("()", ArrayUtils.toString(new Object[]{}, ", ", "(", ")"));
		assertEquals("str1, str2", ArrayUtils.toString(new Object[]{"str1", "str2"}, ", ", "", ""));
		assertEquals("str1/str2", ArrayUtils.toString(new Object[]{"str1", "str2"}, "/", "", ""));
		assertEquals("somestartstr1/str2someend", ArrayUtils.toString(new Object[]{"str1", "str2"}, "/", "somestart", "someend"));

		// verify things mentioned in the javadoc
		assertEquals("null", ArrayUtils.toString(null, null, null, null));
		assertEquals("()", ArrayUtils.toString(new Object[0], null, "(", ")"));
		assertEquals("(a)", ArrayUtils.toString(new Object[] {"a"}, null, "(", ")"));
		assertEquals("(a,b)", ArrayUtils.toString(new Object[] {"a", "b"}, ",", "(", ")"));
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(ArrayUtils.class);
	}
}
