package org.dstadler.commons.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.dstadler.commons.testing.MockRESTServer;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

public class AbstractSimpleContentHandlerTest {
	@Test
	public void testCharacters() {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<>() {

		};

		handler.characters("somechars".toCharArray(), 0, 8);
	}

	@Test
	public void testParseContentURL() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<>() {

		};

		try (MockRESTServer server = new MockRESTServer("200", "text/xml", FileUtils.readFileToString(new File("src/test/data/svnlog.xml"), "UTF-8"))) {
			SortedMap<String, String> map = handler.parseContent(URI.create("http://localhost:" + server.getPort()).toURL(), "", null, 10_000);
			assertTrue(map.isEmpty(), "Parsing not implemented in abstract base class");
		}
	}

	@Test
	public void testParseContentURLParseFails() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<>() {

			@Override
			public SortedMap<String, String> parseContent(InputStream strm) throws IOException {
				throw new IOException("testexception");
			}
		};

		try {
			try (MockRESTServer server = new MockRESTServer("200", "text/xml", FileUtils.readFileToString(new File("src/test/data/svnlog.xml"), "UTF-8"))) {
				handler.parseContent(URI.create("http://localhost:" + server.getPort()).toURL(), "", null, 10_000);
			}
			fail("Should catch exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "testexception");
		}
	}

	@Test
	public void testParseContentURLFails() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<>() {

		};

		try {
			handler.parseContent(URI.create("http://invalidhostname/doesnotexist").toURL(), "", null, 10_000);
			fail("Should catch exception");
		} catch (UnknownHostException e) {
			TestHelpers.assertContains(e, "invalidhostname");	// NOPMD
		} catch (IOException e) {
			// exception only contains details on Windows...
			if(SystemUtils.IS_OS_WINDOWS) {
				TestHelpers.assertContains(e, "invalidhostname", "doesnotexist");	// NOPMD
			}
		}
	}

	@Test
	public void testParseContentURLSyntaxFails() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<>() {

		};

		try {
			handler.parseContent(URI.create("http://inv\"!$%()(§$)(alidhostname/doesnotexist").toURL(), "", null, 10_000);
			fail("Should catch exception");
		} catch (IllegalArgumentException e) {
            TestHelpers.assertContains(e, "inv\"!$%()(§$)(alidhostname");    // NOPMD
		}
	}

	@Test
	public void testParseContentNotFound() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<>() {

		};

		try (MockRESTServer server = new MockRESTServer("404", "text/xml", FileUtils.readFileToString(new File("src/test/data/svnlog.xml"), "UTF-8"))) {
		    try {
    			handler.parseContent(URI.create("http://localhost:" + server.getPort() + "/notfound").toURL(), "", null, 10_000);
    			fail("Should catch exception");
    		} catch (IOException e) {
    			TestHelpers.assertContains(e, "http://localhost:" + server.getPort() + "/notfound");
    		}
		}
	}

	@Test
	public void testParseContentInputStream() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<>() {

		};

		handler.parseContent(new ByteArrayInputStream("<file><entry/></file>".getBytes()));
	}

	@Test
	public void testErrorSAXParseException() {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<>() {

		};

		handler.error(new SAXParseException("testexception", null));
	}

	@Test
	public void testFatalErrorSAXParseException() {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<>() {

		};

		handler.fatalError(new SAXParseException("testexception", null));
	}

	@Test
	public void testWarningSAXParseException() {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<>() {

		};

		handler.warning(new SAXParseException("testexception", null));
	}

	@Test
	public void testGetConfigs() {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<>() {

		};
		assertEquals(0, handler.getConfigs().size());
	}

}
