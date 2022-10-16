package org.dstadler.commons.arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

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

	    // from the JavaDoc
		assertEquals("null", ArrayUtils.toString(null, "abc"));
		assertEquals("[]", ArrayUtils.toString(new Object[0], "abc"));
		assertEquals("[a]", ArrayUtils.toString(new Object[] {"a"}, "abc"));
		assertEquals("[a;b]", ArrayUtils.toString(new Object[] {"a", "b"}, ";"));
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
		assertEquals("a,b)", ArrayUtils.toString(new Object[] {"a", "b"}, ",", "", ")"));
		assertEquals("a,b", ArrayUtils.toString(new Object[] {"a", "b"}, ",", "", ""));
		assertEquals("a, b", ArrayUtils.toString(new Object[] {"a", "b"}, ", ", "", ""));
	}

	@Test
	public void testToStringObjectArrayStringStringStringInvalid() {
		// check passing in invalid null as delimiter, suffix or prefix
		assertThrows(NullPointerException.class,
				() -> ArrayUtils.toString(new Object[] {"a", "b"}, null, "", ""));
		assertThrows(NullPointerException.class,
				() -> ArrayUtils.toString(new Object[] {"a", "b"}, ", ", null, ""));
		assertEquals("a, 1null", ArrayUtils.toString(new Object[] {"a", "1"}, ", ", "", null));
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(ArrayUtils.class);
	}
}
