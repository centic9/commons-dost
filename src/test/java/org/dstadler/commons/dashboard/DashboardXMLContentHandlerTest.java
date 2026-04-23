package org.dstadler.commons.dashboard;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

import org.dstadler.commons.testing.TestHelpers;

public class DashboardXMLContentHandlerTest {

	@Test
	public void testParseContent() throws Exception {
		Map<String, String> handler =
			DashboardXMLContentHandler.parseContent(new ByteArrayInputStream("<xml><dashboard id=\"id1\" href=\"href1\"/></xml>".getBytes()));
		assertNotNull(handler);
		assertEquals(1, handler.size());
		assertTrue(handler.containsKey("id1"));
		assertEquals("href1", handler.get("id1"));
	}

	@Test
	public void testStartElement() throws Exception {
		DashboardXMLContentHandler handler = new DashboardXMLContentHandler();

		handler.fatalError(new SAXParseException("messag", null));
		handler.error(new SAXParseException("messag", null));
		handler.warning(new SAXParseException("messag", null));

		assertNotNull(handler.getDashboards());
		assertEquals(0, handler.getDashboards().size());

		// nothing happens if not "dashboard"
		handler.startElement("", "", "", null);

		// exception if required attributes not found
		SAXException e = assertThrows(SAXException.class,
				() -> handler.startElement("", "dashboard", "dashboard", new AttributesImpl()));
		TestHelpers.assertContains(e, "Did not have id and href ");

		assertEquals(0, handler.getDashboards().size());

		AttributesImpl att = new AttributesImpl();
		att.addAttribute("", "id", "id", "", "someid");
		att.addAttribute("", "href", "href", "", "somehref");
		handler.startElement("", "dashboard", "dashboard", att);
		assertEquals(1, handler.getDashboards().size());

		AttributesImpl emptyId = new AttributesImpl();
		emptyId.addAttribute("", "id", "id", "", "");
		emptyId.addAttribute("", "href", "href", "", "somehref");
		e = assertThrows(SAXException.class,
				() -> handler.startElement("", "dashboard", "dashboard", emptyId));
		TestHelpers.assertContains(e, "Did not have id and href on dashboard-tag.");

		AttributesImpl emptyHref = new AttributesImpl();
		emptyHref.addAttribute("", "id", "id", "", "someid");
		emptyHref.addAttribute("", "href", "href", "", "");
		e = assertThrows(SAXException.class,
				() -> handler.startElement("", "dashboard", "dashboard", emptyHref));
		TestHelpers.assertContains(e, "Did not have id and href on dashboard-tag.");

		AttributesImpl nullId = new AttributesImpl();
		nullId.addAttribute("", "id", "id", "", null);
		nullId.addAttribute("", "href", "href", "", "somehref");
		e = assertThrows(SAXException.class,
				() -> handler.startElement("", "dashboard", "dashboard", nullId));
		TestHelpers.assertContains(e, "Did not have id and href on dashboard-tag.");

		AttributesImpl nullHref = new AttributesImpl();
		nullHref.addAttribute("", "id", "id", "", "someid");
		nullHref.addAttribute("", "href", "href", "", null);
		e = assertThrows(SAXException.class,
				() -> handler.startElement("", "dashboard", "dashboard", nullHref));
		TestHelpers.assertContains(e, "Did not have id and href on dashboard-tag.");
	}

}
