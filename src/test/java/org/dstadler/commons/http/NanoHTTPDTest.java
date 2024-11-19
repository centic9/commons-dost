package org.dstadler.commons.http;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dstadler.commons.http.NanoHTTPD.Response;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.net.SocketUtils;
import org.dstadler.commons.net.UrlUtils;
import org.dstadler.commons.testing.MockRESTServer;
import org.dstadler.commons.testing.TestHelpers;
import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class NanoHTTPDTest {
	@BeforeAll
	public static void setUpClass() throws Exception {
		LoggerFactory.initLogging();
	}

	@AfterEach
	public void tearDown() throws InterruptedException {
		ThreadTestHelper.waitForThreadToFinishSubstring("NanoHTTP");
	}

	@Test
    void testServe() throws Exception {
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
    void testServeWithHeader() throws Exception {
		int port = SocketUtils.getNextFreePort(9000, 9010);
        NanoHTTPD httpd = new NanoHTTPD(port);
		try {

            final URL url = new URL("http://localhost:" + port);

            final URLConnection con;
            con = url.openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            con.addRequestProperty("Test", "value");
            assertNotEquals(-1, con.getInputStream().read());
        } finally {
            httpd.stop();
        }
	}

	@Test
    void testServeWithInvalidHeader() throws Exception {
		int port = SocketUtils.getNextFreePort(9000, 9010);
        NanoHTTPD httpd = new NanoHTTPD(port);
		try {
            try (Socket socket = new Socket("localhost", port)) {
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);

                out.println("""
                        GET /example/file.html?query=123 prop1:prop2
                        Content-Type: blabla
                        InvalidHeader
                        
                        Content""");

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                IOUtils.copy(socket.getInputStream(), bytes);

                System.out.println("\nReply: " + bytes.toString(StandardCharsets.UTF_8));
                String reply = bytes.toString(StandardCharsets.UTF_8);
                TestHelpers.assertContains(reply, "404", "Content-Type", "text/plain");
            }
        } finally {
            httpd.stop();
        }
	}

	@Test
    void testServeFile() throws Exception {
		int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port);

		assertEquals(
				NanoHTTPD.HTTP_INTERNALERROR,
				httpd.serveFile(null, null, new File("somefile"), true).status,
				"Should fail when not a directory.");

		assertEquals(
				NanoHTTPD.HTTP_FORBIDDEN,
				httpd.serveFile("../build.gradle", null, new File("."), true).status,
				"Should not serve path with ..");

		assertEquals(
				NanoHTTPD.HTTP_FORBIDDEN,
				httpd.serveFile("build.gradle/..", null, new File("."), true).status,
				"Should not serve path with ..");

		assertEquals(
				NanoHTTPD.HTTP_FORBIDDEN,
				httpd.serveFile("somepath/../build.gradle", null, new File("."), true).status,
				"Should not serve path with ..");

		assertEquals(
				NanoHTTPD.HTTP_NOTFOUND,
				httpd.serveFile("somepath", null, new File("."), true).status,
				"File not found..");

		assertEquals(
				NanoHTTPD.HTTP_OK,
				httpd.serveFile("build.gradle?param=1", new Properties(), new File("."), true).status,
				"File is found");

        assertEquals(
				NanoHTTPD.HTTP_OK,
				httpd.serveFile(".gitignore", new Properties(), new File("."), true).status,
				"File is found");

        assertEquals(
				NanoHTTPD.HTTP_OK,
				httpd.serveFile("gradlew", new Properties(), new File("."), true).status,
				"File is found");

        assertEquals(
				NanoHTTPD.HTTP_OK,
				httpd.serveFile("src/test/data/empty.txt", new Properties(), new File("."), true).status,
				"File is found");

        Properties header = new Properties();
        header.put("range", "something");
        assertEquals(NanoHTTPD.HTTP_OK,
				httpd.serveFile("build.gradle?param=1", header, new File("."), true).status,
				"File is found");

        header.put("range", "bytes=12");
        assertEquals(
				NanoHTTPD.HTTP_OK, httpd.serveFile("build.gradle?param=1", header, new File("."), true).status,
				"File is found");

        header.put("range", "bytes=-121234");
        assertEquals(
				NanoHTTPD.HTTP_OK,
				httpd.serveFile("build.gradle?param=1", header, new File("."), true).status,
				"File is found");

        header.put("range", "bytes=123-121234");
        assertEquals(
				NanoHTTPD.HTTP_OK,
				httpd.serveFile("build.gradle?param=1", header, new File("."), true).status,
				"File is found");

        header.put("range", "bytes=illegal");
        assertEquals(
				NanoHTTPD.HTTP_OK,
				httpd.serveFile("build.gradle?param=1", header, new File("."), true).status,
				"File is found");

        httpd.stop();
	}

	@Test
    void testServeSuper() throws IOException {
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
    void testServeException() throws IOException {
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
    void testResponse() {
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
    void testPortOutOfRange() throws IOException {
		try {
			NanoHTTPD server = new NanoHTTPD(128000);
			assertNotNull(server);
			fail("Should not be possible to assign port out of range");
		} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
			// expected
		}
	}

	@Disabled("may not work on all machines")
	@Test
    void testPortPreismonitor() throws IOException {
		NanoHTTPD server = new NanoHTTPD(10080);
		try {
			assertNotNull(server);
		} finally {
			server.stop();
		}
	}

    @Test
    void testNoErrorLogDuringShutdown() throws IOException {
    	// port 9004 is hardcoded in Instance.Test
    	NanoHTTPD server = new NanoHTTPD(SocketUtils.getNextFreePort(9000, 9010));
		server.stop();
    }

	@Test
    void testServeTimeoutInitial() throws Exception {
		int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port, null, 1_000);

		try (Socket socket = new Socket("localhost", port)) {
			// wait some time to trigger the timeout
			Thread.sleep(2000);

			assertTrue(IOUtils.toString(socket.getInputStream(), StandardCharsets.UTF_8).startsWith("HTTP/1.0 500 Internal Server Error"));
		}

		httpd.stop();
	}

	@Test
    void testServeTimeoutStarted() throws Exception {
		int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port, null, 1_000);

		try (Socket socket = new Socket("localhost", port)) {
			// write some bits
			socket.getOutputStream().write("POST index.html\n".getBytes(StandardCharsets.UTF_8));

			// wait some time to trigger the timeout
			Thread.sleep(2000);

			assertTrue(IOUtils.toString(socket.getInputStream(), StandardCharsets.UTF_8).startsWith("HTTP/1.0 500 Internal Server Error"));
		}

		httpd.stop();
	}

	@Test
    void testServeInvalidBindName() throws Exception {
		int port = SocketUtils.getNextFreePort(9000, 9010);
		try {
			NanoHTTPD nanoHTTPD = new NanoHTTPD(port, InetAddress.getByName("192.168.123.234"));
			assertNotNull(nanoHTTPD);
			fail("Should catch exception here");
		} catch (@SuppressWarnings("unused") BindException e) {
			// expected to an exception here
		}
	}

	@Test
    void testEncoding() throws IOException {
		NanoHTTPD.setEncoding("UTF-8");
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html>\u00E4</html>")) {
			String data;

			// this test can only run for UTF-8
			if(Charset.defaultCharset().equals(StandardCharsets.UTF_8)) {
				data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10_000);
				assertEquals("<html>\u00E4</html>", data,
						"Failed whit default charset: " + Charset.defaultCharset());
			}

			data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), "UTF-8", 10_000);
			assertEquals("<html>\u00E4</html>", data);

			data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), "ISO-8859-1", 10_000);
			assertEquals("<html>\u00c3\u00a4</html>", data);
		} finally {
			NanoHTTPD.setEncoding(null);
		}
	}

	@Test
    void testInvalidEncoding() throws IOException {
		NanoHTTPD.setEncoding("SomeInvalidEncoding");
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html>\u00E4</html>")) {
			String data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10_000);
			assertEquals("", data);

			data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), "UTF-8", 10_000);
			assertEquals("", data);

			data = UrlUtils.retrieveData("http://localhost:" + server.getPort(), "ISO-8859-1", 10_000);
			assertEquals("", data);
		} finally {
			NanoHTTPD.setEncoding(null);
		}
	}

	@Test
    void testServerTwice() throws IOException {
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
		} catch (@SuppressWarnings("unused") BindException e) {
			// Error is locale-specific: TestHelpers.assertContains(e, "Address already in use");
		} finally {
			httpd.stop();
		}
	}

	@Test
    void testRegexSearchParserSSLException()  throws Exception {
		try (MockRESTServer server = new MockRESTServer("200 OK", NanoHTTPD.MIME_HTML + "; charset=UTF-8", "")) {
			try {
				retrieveData("https://localhost:" + server.getPort());
				fail("Should catch exception because SSL does not work in NanoHTTP");
			} catch (SSLException e) {
				String str = e.getMessage();
				assertTrue(str.contains("SSL message") || str.contains("Remote host terminated the handshake"),
						". Expected to find an SSL error message, but was not contained in provided string '" + str +
								"'\n" + ExceptionUtils.getStackTrace(e));
			} catch (SocketTimeoutException e) {
				assertTrue(SystemUtils.IS_OS_WINDOWS,
						"On Windows closing the socket does not wake up the thread in NanoHTTP when it is blocked reading properties");
			}
		}
	}

	private static void retrieveData(String sUrl) throws IOException {
		URL url = new URL(sUrl);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {
			//Authenticator.setDefault(new SimpleAuthenticator(SERVER_USER, SERVER_PASSWORD));

			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);

			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.connect();
			int code = conn.getResponseCode();
			if (code >= HttpURLConnection.HTTP_NO_CONTENT) {
				String msg = "Error " + code + " returned while retrieving response for url " + url
						+ " message from client: " + conn.getResponseMessage();
				throw new IOException(msg);
			}
		} finally {
			conn.disconnect();
		}
	}
}
