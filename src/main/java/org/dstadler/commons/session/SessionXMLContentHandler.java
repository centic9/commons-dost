package org.dstadler.commons.session;

import java.util.logging.Logger;

import org.xml.sax.SAXException;

import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.xml.AbstractSimpleContentHandler;

/**
 * @author dominik.stadler
 *
 */
public class SessionXMLContentHandler extends AbstractSimpleContentHandler<String, String> {
	private final static Logger log = LoggerFactory.make();

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(localName.equals("sessionid")) {
			String session = characters.toString();
			configs.put(session, session);

			log.info("Found Session '" + session + "'");	// with href: " + atts.getValue("href")
		}
		characters.setLength(0);
	}
}
