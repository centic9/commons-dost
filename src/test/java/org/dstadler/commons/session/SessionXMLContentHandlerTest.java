package org.dstadler.commons.session;

import static org.junit.Assert.assertEquals;

import java.util.TreeMap;

import org.junit.Test;

public class SessionXMLContentHandlerTest {

	@Test
	public void testEndElementStringStringString() {
		SessionXMLContentHandler handler = new SessionXMLContentHandler();

		handler.endElement("some", "unknown", null);

		assertEquals(0, handler.getConfigs().size());

		handler.characters("somechars".toCharArray(), 0, 8);
		handler.endElement(null, "sessionid", null);
		assertEquals(1, handler.getConfigs().size());
		assertEquals("{somechar=somechar}", handler.getConfigs().toString());

		// a second time to verify resetting of length
		handler.characters("somechars".toCharArray(), 0, 8);
		handler.endElement(null, "sessionid", null);
		assertEquals(1, handler.getConfigs().size());
		assertEquals("{somechar=somechar}", handler.getConfigs().toString());

		// add a second config
		handler.characters("otherchars".toCharArray(), 0, 10);
		handler.endElement(null, "sessionid", null);
		assertEquals(2, handler.getConfigs().size());
		assertEquals("{otherchars=otherchars, somechar=somechar}",
				// TreeMap to sort by key
				new TreeMap<>(handler.getConfigs()).toString());
	}
}
