package org.dstadler.commons.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Can read some information off of the list of Dashboards returned by the dynaTrace XML reporting.
 *
 * @author dominik.stadler
 *
 */
public class DashboardXMLContentHandler extends DefaultHandler {
	private final static Logger log = Logger.getLogger(DashboardXMLContentHandler.class.getName());

	// use TreeMap to sort it by dashboard-name
	private Map<String, String> dashboards = new TreeMap<>();

	public static Map<String, String> parseContent(InputStream strm) throws SAXException, IOException {
		XMLReader parser = XMLReaderFactory.createXMLReader();

		DashboardXMLContentHandler handler = new DashboardXMLContentHandler();

		parser.setContentHandler(handler);
		parser.setErrorHandler(handler);

		InputSource source = new InputSource(strm);

		parser.parse(source);

		return handler.getDashboards();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if(localName.equals("dashboard")) {
			if(atts.getValue("id") == null || atts.getValue("href") == null ||
					atts.getValue("id").isEmpty() || atts.getValue("href").isEmpty()) {
				throw new SAXException("Did not have id and href on dashboard-tag.");
			}

			dashboards.put(atts.getValue("id"), atts.getValue("href"));
			log.info("Found Dashboard '" + atts.getValue("id") + "' with href: " + atts.getValue("href"));
		}
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		log.log(Level.SEVERE, "Error in SAX Parsing", exception);
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		log.log(Level.SEVERE, "Error in SAX Parsing", exception);
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		log.log(Level.WARNING, "Error in SAX Parsing", exception);
	}

	/**
	 * @return the issues
	 */
	public Map<String, String> getDashboards() {
		return dashboards;
	}
}
