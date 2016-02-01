package org.dstadler.commons.http;

import org.apache.commons.io.IOUtils;
import org.dstadler.commons.http.NanoHTTPD.Response;
import org.dstadler.commons.net.SocketUtils;
import org.dstadler.commons.net.UrlUtils;
import org.dstadler.commons.testing.MockRESTServer;
import org.dstadler.commons.testing.TestHelpers;
import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class NanoHTTPDTest {
	@After
	public void tearDown() throws InterruptedException {
		ThreadTestHelper.waitForThreadToFinishSubstring("NanoHTTP");
	}

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

	@Test
	public void testServeTimeoutInitial() throws Exception {
		int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port, null, 1_000);

		Socket socket = new Socket("localhost", port);

		// wait some time to trigger the timeout
		Thread.sleep(2000);

		assertTrue(IOUtils.toString(socket.getInputStream()).startsWith("HTTP/1.0 500 Internal Server Error"));

		httpd.stop();
	}

	@Test
	public void testServeTimeoutStarted() throws Exception {
		int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port, null, 1_000);

		Socket socket = new Socket("localhost", port);

		// write some bits
		socket.getOutputStream().write("POST index.html\n".getBytes("UTF-8"));

		// wait some time to trigger the timeout
		Thread.sleep(2000);

		assertTrue(IOUtils.toString(socket.getInputStream()).startsWith("HTTP/1.0 500 Internal Server Error"));

		httpd.stop();
	}

	@Test
	public void testServeInvalidBindname() throws Exception {
		int port = SocketUtils.getNextFreePort(9000, 9010);
		try {
			new NanoHTTPD(port, InetAddress.getByName("192.168.123.234"));
		} catch (BindException e) {
			// expected to an exception here
		}
	}

	@Test
	public void testEncoding() throws IOException {
		NanoHTTPD.setEncoding("UTF-8");
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html>\u00E4</html>")) {
			String data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10_000);
			assertEquals("<html>\u00E4</html>", data);

			data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), "UTF-8", 10_000);
			assertEquals("<html>\u00E4</html>", data);

			data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), "ISO-8859-1", 10_000);
			assertEquals("<html>\u00c3\u00a4</html>", data);
		}
	}

	@Test
	public void testInvalidEncoding() throws IOException {
		NanoHTTPD.setEncoding("SomeInvalidEncoding");
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html>\u00E4</html>")) {
			String data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10_000);
			assertEquals("", data);

			data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), "UTF-8", 10_000);
			assertEquals("", data);

			data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), "ISO-8859-1", 10_000);
			assertEquals("", data);
		}
	}

	@Test
	public void testServerTwice() throws IOException {
		final int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port) {
			@Override
			public Response serve(String uri, String method, Properties header, Properties parms) {
				return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "Ok");
			}
		};

		try {
			NanoHTTPD httpd2 = new NanoHTTPD(port) {
				@Override
				public Response serve(String uri, String method, Properties header, Properties parms) {
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "Ok");
				}
			};
			httpd2.stop();
		} catch (BindException e) {
			TestHelpers.assertContains(e, "Address already in use");
		}

		httpd.stop();
	}
}
