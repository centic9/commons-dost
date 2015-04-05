package org.apache.dstadler.test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;


/**
 *
 * @author dominik.stadler
 */
public class TestSAXParserFactory extends SAXParserFactory {

	/* (non-Javadoc)
	 * @see javax.xml.parsers.SAXParserFactory#newSAXParser()
	 */
	@Override
	public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {

		return null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.parsers.SAXParserFactory#setFeature(java.lang.String, boolean)
	 */
	@Override
	public void setFeature(String name, boolean value) throws ParserConfigurationException, SAXNotRecognizedException,
			SAXNotSupportedException {


	}

	/* (non-Javadoc)
	 * @see javax.xml.parsers.SAXParserFactory#getFeature(java.lang.String)
	 */
	@Override
	public boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException,
			SAXNotSupportedException {

		return false;
	}

}
