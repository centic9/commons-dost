package org.dstadler.commons.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple wrapper to allow to iterate over a NodeList.
 *
 * Note: some methods are not implemented yet, only the most basic ones are done currently.
 * Also this is a read-only List, so any method that would change the List is not supported as well.
 *
 * @author dominik.stadler
 *
 */
public class NodeListWrapper implements List<Node> {
	private final class LocalIterator implements Iterator<Node> {
		int nextPos = 0;

		@Override
		public boolean hasNext() {
			return nodeList.getLength() > nextPos;
		}

		@Override
		public Node next() {
			if(nextPos >= nodeList.getLength()) {
				throw new NoSuchElementException("Cannot access beyond end of iterator, had " + nodeList.getLength() + " items, tried to access item at position " + nextPos + ".");
			}

			Node node = nodeList.item(nextPos);
			nextPos++;
			return node;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private final NodeList nodeList;

	public NodeListWrapper(NodeList nodeList) {
		super();
		this.nodeList = nodeList;
	}

	@Override
	public int size() {
		return nodeList.getLength();
	}

	@Override
	public boolean isEmpty() {
		return nodeList.getLength() == 0;
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Node> iterator() {
		return new LocalIterator();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(Node e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Node> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends Node> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Node get(int index) {
		return nodeList.item(index);
	}

	@Override
	public Node set(int index, Node element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, Node element) {
		throw new UnsupportedOperationException();

	}

	@Override
	public Node remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<Node> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<Node> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Node> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}
}
