package org.dstadler.commons.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.dstadler.commons.testing.MockRESTServer;
import org.dstadler.commons.testing.TestHelpers;

public class AbstractSimpleContentHandlerTest {
	@Test
	public void testCharacters() throws SAXException {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<String, String>() {
		};

		handler.characters("somechars".toCharArray(), 0, 8);
	}

	@Test
	public void testParseContentURL() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<String, String>() {
		};

		try (MockRESTServer server = new MockRESTServer("200", "text/xml", FileUtils.readFileToString(new File("src/test/data/svnlog.xml")))) {
			handler.parseContent(new URL("http://localhost:" + server.getPort()), "", null, 10_000);
		}
	}

	@Test
	public void testParseContentURLParseFails() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<String, String>() {
			@Override
			public SortedMap<String, String> parseContent(InputStream strm) throws SAXException, IOException {
				throw new IOException("testexception");
			}
		};

		try {
			try (MockRESTServer server = new MockRESTServer("200", "text/xml", FileUtils.readFileToString(new File("src/test/data/svnlog.xml")))) {
				handler.parseContent(new URL("http://localhost:" + server.getPort()), "", null, 10_000);
			}
			fail("Should catch exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "testexception");
		}
	}

	@Test
	public void testParseContentURLFails() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<String, String>() {
		};

		try {
			handler.parseContent(new URL("http://invalidhostname/doesnotexist"), "", null, 10_000);
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
	public void testParseContentNotFound() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<String, String>() {
		};

		try (MockRESTServer server = new MockRESTServer("404", "text/xml", FileUtils.readFileToString(new File("src/test/data/svnlog.xml")))) {
		    try {
    			handler.parseContent(new URL("http://localhost:" + server.getPort() + "/notfound"), "", null, 10_000);
    			fail("Should catch exception");
    		} catch (IOException e) {
    			TestHelpers.assertContains(e, "http://localhost:" + server.getPort() + "/notfound");
    		}
		}
	}

	@Test
	public void testParseContentInputStream() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<String, String>() {
		};

		handler.parseContent(new ByteArrayInputStream("<file><entry/></file>".getBytes()));
	}

	@Test
	public void testErrorSAXParseException() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<String, String>() {
		};

		handler.error(new SAXParseException("testexception", null));
	}

	@Test
	public void testFatalErrorSAXParseException() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<String, String>() {
		};

		handler.fatalError(new SAXParseException("testexception", null));
	}

	@Test
	public void testWarningSAXParseException() throws Exception {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<String, String>() {
		};

		handler.warning(new SAXParseException("testexception", null));
	}

	@Test
	public void testGetConfigs() {
		AbstractSimpleContentHandler< String, String> handler = new AbstractSimpleContentHandler<String, String>() {
		};
		assertEquals(0, handler.getConfigs().size());
	}

}
