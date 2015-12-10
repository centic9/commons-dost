package org.dstadler.commons.http;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.dstadler.commons.http.NanoHTTPD.Response;
import org.dstadler.commons.net.SocketUtils;
import org.dstadler.commons.net.UrlUtils;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.Ignore;
import org.junit.Test;

public class NanoHTTPDTest {

	@Test
	public void testServe() throws Exception {
		int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port);
// {

//			@Override
//			public Response serve(String uri, String method, Properties header,
//					Properties parms) {
//				return super.serve(uri, method, header, parms);
//			}

//		};

		// verify that we can access the url
		assertTrue(Utils.getURL("http://localhost:" + port, new AtomicInteger(1), 1));

		httpd.stop();
	}

	@Test
	public void testServeFile() throws Exception {
		int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port);

		assertEquals("Should fail when not a directory.",
				NanoHTTPD.HTTP_INTERNALERROR,
				httpd.serveFile(null, null, new File("somefile"), true).status);

		assertEquals("Should not serve path with ..",
				NanoHTTPD.HTTP_FORBIDDEN,
				httpd.serveFile("../build.gradle", null, new File("."), true).status);

		assertEquals("Should not serve path with ..",
				NanoHTTPD.HTTP_FORBIDDEN,
				httpd.serveFile("build.gradle/..", null, new File("."), true).status);

		assertEquals("Should not serve path with ..",
				NanoHTTPD.HTTP_FORBIDDEN,
				httpd.serveFile("somepath/../build.gradle", null, new File("."), true).status);

		assertEquals("File not found..",
				NanoHTTPD.HTTP_NOTFOUND,
				httpd.serveFile("somepath", null, new File("."), true).status);

		assertEquals("File is found",
				NanoHTTPD.HTTP_OK,
				httpd.serveFile("build.gradle?param=1", new Properties(), new File("."), true).status);

        assertEquals("File is found",
                NanoHTTPD.HTTP_OK,
                httpd.serveFile(".gitignore", new Properties(), new File("."), true).status);

        assertEquals("File is found",
                NanoHTTPD.HTTP_OK,
                httpd.serveFile("gradlew", new Properties(), new File("."), true).status);

        assertEquals("File is found",
                NanoHTTPD.HTTP_OK,
                httpd.serveFile("src/test/data/empty.txt", new Properties(), new File("."), true).status);

        Properties header = new Properties();
        header.put("range", "something");
        assertEquals("File is found",
                NanoHTTPD.HTTP_OK,
                httpd.serveFile("build.gradle?param=1", header, new File("."), true).status);

        header.put("range", "bytes=12");
        assertEquals("File is found",
                NanoHTTPD.HTTP_OK,
                httpd.serveFile("build.gradle?param=1", header, new File("."), true).status);

        header.put("range", "bytes=-121234");
        assertEquals("File is found",
                NanoHTTPD.HTTP_OK,
                httpd.serveFile("build.gradle?param=1", header, new File("."), true).status);

        header.put("range", "bytes=123-121234");
        assertEquals("File is found",
                NanoHTTPD.HTTP_OK,
                httpd.serveFile("build.gradle?param=1", header, new File("."), true).status);

        header.put("range", "bytes=illegal");
        assertEquals("File is found",
                NanoHTTPD.HTTP_OK,
                httpd.serveFile("build.gradle?param=1", header, new File("."), true).status);

        httpd.stop();
	}

	@Test
	public void testServeSuper() throws IOException {
		final int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port) {
			@Override
			public Response serve(String uri, String method, Properties header, Properties parms) {
				header.setProperty("somekey", "somevalue");
				parms.setProperty("param1", "value1");
				return super.serve(uri, method, header, parms);
			}
		};

		try {
			assertTrue(Utils.getURL("http://localhost:" + port, new AtomicInteger(100), 2));
		} finally {
			httpd.stop();
		}
	}

	@Test
	public void testServeException() throws IOException {
		final int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port) {
			@Override
			public Response serve(String uri, String method, Properties header, Properties parms) {
				throw new RuntimeException("Testexception");
			}
		};

		try {
			String accessError = UrlUtils.getAccessError("http://localhost:" + port, true, false, 10000000);
			TestHelpers.assertContains(accessError, "500");
		} finally {
			httpd.stop();
		}
	}

	@Test
	public void testResponse() {
		Response response = new Response();
		assertEquals(NanoHTTPD.HTTP_OK, response.status);
		assertNull(response.mimeType);
		assertNull(response.data);

		response = new Response(NanoHTTPD.HTTP_BADREQUEST, NanoHTTPD.MIME_HTML, (InputStream)null);
		assertEquals(NanoHTTPD.HTTP_BADREQUEST, response.status);
		assertEquals(NanoHTTPD.MIME_HTML, response.mimeType);

		response.addHeader("somename", "othervalue");
		assertEquals("othervalue", response.header.getProperty("somename"));
	}

	@Test
	public void testPortOutOfRange() throws IOException {
		try {
			NanoHTTPD server = new NanoHTTPD(128000);
			assertNotNull(server);
			fail("Should not be possible to assign port out of range");
		} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
			// expected
		}
	}

	@Ignore("may not work on all machines")
	@Test
	public void testPortPreismonitor() throws IOException {
		NanoHTTPD server = new NanoHTTPD(10080);
		try {
			assertNotNull(server);
		} finally {
			server.stop();
		}
	}

    @Test
    public void testNoErrorLogDuringShutdown() throws IOException {
    	// port 9004 is hardcoded in Instance.Test
    	NanoHTTPD server = new NanoHTTPD(SocketUtils.getNextFreePort(9000, 9010));
		server.stop();
    }
}
