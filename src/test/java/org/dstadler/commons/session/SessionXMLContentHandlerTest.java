package org.dstadler.commons.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xml.sax.SAXException;

public class SessionXMLContentHandlerTest {

	@Test
	public void testEndElementStringStringString() throws SAXException {
		SessionXMLContentHandler handler = new SessionXMLContentHandler();

		handler.endElement("some", "unknown", null);

		assertEquals(0, handler.getConfigs().size());

		handler.characters("somechars".toCharArray(), 0, 8);
		handler.endElement(null, "sessionid", null);
		assertEquals(1, handler.getConfigs().size());
		assertTrue(handler.getConfigs().containsKey("somechar"));
	}
}
