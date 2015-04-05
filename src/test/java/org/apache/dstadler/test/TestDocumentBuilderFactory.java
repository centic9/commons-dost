package org.apache.dstadler.test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 *
 * @author dominik.stadler
 */
public class TestDocumentBuilderFactory extends DocumentBuilderFactory {

	/* (non-Javadoc)
	 * @see javax.xml.parsers.DocumentBuilderFactory#newDocumentBuilder()
	 */
	@Override
	public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {

		return null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.parsers.DocumentBuilderFactory#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute(String name, Object value) throws IllegalArgumentException {


	}

	/* (non-Javadoc)
	 * @see javax.xml.parsers.DocumentBuilderFactory#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String name) throws IllegalArgumentException {

		return null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.parsers.DocumentBuilderFactory#setFeature(java.lang.String, boolean)
	 */
	@Override
	public void setFeature(String name, boolean value) throws ParserConfigurationException {


	}

	/* (non-Javadoc)
	 * @see javax.xml.parsers.DocumentBuilderFactory#getFeature(java.lang.String)
	 */
	@Override
	public boolean getFeature(String name) throws ParserConfigurationException {

		return false;
	}

}
