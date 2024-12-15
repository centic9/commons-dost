package org.dstadler.commons.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dstadler.commons.testing.TestHelpers;

public class NodeListWrapperTest {
	@Test
	public void testNodeListWrapper() {
		NodeListWrapper wrapper = new NodeListWrapper(new EmptyNodeList());
		assertEquals(0, wrapper.size());
		assertTrue(wrapper.isEmpty());

		wrapper = new NodeListWrapper(new CountNodeList(23));
		assertEquals(23, wrapper.size());
		assertFalse(wrapper.isEmpty());
		for(Node node : wrapper) {
			assertNull(node);
		}

		assertNull(wrapper.get(2));
	}

	@Test
	public void testNodeListWrapperReadOverTheEnd() {
		NodeListWrapper wrapper = new NodeListWrapper(new CountNodeList(23));
		assertEquals(23, wrapper.size());
		assertFalse(wrapper.isEmpty());

		Iterator<Node> it = wrapper.iterator();

		while(it.hasNext()) {
			assertNull(it.next());
		}

		// ensure that we get an error if we access out of bounds
		try {
			it.next();
		} catch (NoSuchElementException e) {
			TestHelpers.assertContains(e, "Cannot access beyond end of iterator", "23");
		}

		try {
			wrapper.add(null);
			fail("Add should not be supported");
		} catch (UnsupportedOperationException e) {
			// expected here
		}
	}

	@Test
	public void testNodeListWrapperUnsupportedOperations() {
		NodeListWrapper wrapper = new NodeListWrapper(new CountNodeList(12));

		try {
			wrapper.iterator().remove();
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			assertTrue(wrapper.contains(null));
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.toArray();
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.toArray(new String[] {});
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.add(null);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.remove(null);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.addAll(null);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.containsAll(null);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.addAll(2, null);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.removeAll(null);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.retainAll(null);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.clear();
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.set(1, null);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.add(3, null);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.remove(2);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.indexOf(null);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.lastIndexOf(null);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.listIterator();
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.listIterator(4);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		try {
			wrapper.subList(2, 6);
			fail("should throw exception");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}
	}

	private static final class CountNodeList implements NodeList {
		int count;

		public CountNodeList(int count) {
			super();
			this.count = count;
		}

		@Override
		public Node item(int index) {
			return null;
		}

		@Override
		public int getLength() {
			return count;
		}
	}

	private static final class EmptyNodeList implements NodeList {
		@Override
		public Node item(int index) {
			throw new IllegalStateException();
		}

		@Override
		public int getLength() {
			return 0;
		}
	}
}
