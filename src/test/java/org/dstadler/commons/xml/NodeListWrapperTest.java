package org.dstadler.commons.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dstadler.commons.testing.TestHelpers;

public class NodeListWrapperTest {
	private static void assertUnsupported(Executable op) {
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, op);
		assertNull(e.getMessage());
	}

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
		NoSuchElementException nse = assertThrows(NoSuchElementException.class, it::next);
		TestHelpers.assertContains(nse, "Cannot access beyond end of iterator", "23");

		assertThrows(UnsupportedOperationException.class, () -> wrapper.add(null),
				"Add should not be supported");
	}

	@Test
	public void testNodeListWrapperUnsupportedOperations() {
		NodeListWrapper wrapper = new NodeListWrapper(new CountNodeList(12));

		assertUnsupported(() -> wrapper.iterator().remove());
		assertUnsupported(() -> wrapper.contains(null));
		assertUnsupported(wrapper::toArray);
		assertUnsupported(() -> wrapper.toArray(new String[] {}));
		assertUnsupported(() -> wrapper.add(null));
		assertUnsupported(() -> wrapper.remove(null));
		assertUnsupported(() -> wrapper.addAll(null));
		assertUnsupported(() -> wrapper.containsAll(null));
		assertUnsupported(() -> wrapper.addAll(2, null));
		assertUnsupported(() -> wrapper.removeAll(null));
		assertUnsupported(() -> wrapper.retainAll(null));
		assertUnsupported(wrapper::clear);
		assertUnsupported(() -> wrapper.set(1, null));
		assertUnsupported(() -> wrapper.add(3, null));
		assertUnsupported(() -> wrapper.remove(2));
		assertUnsupported(() -> wrapper.indexOf(null));
		assertUnsupported(() -> wrapper.lastIndexOf(null));
		assertUnsupported(wrapper::listIterator);
		assertUnsupported(() -> wrapper.listIterator(4));
		assertUnsupported(() -> wrapper.subList(2, 6));
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
