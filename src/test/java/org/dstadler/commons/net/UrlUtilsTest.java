package org.dstadler.commons.net;

import org.dstadler.commons.http.NanoHTTPD;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.testing.MockRESTServer;
import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.dstadler.commons.testing.TestHelpers;
import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.After;
import org.junit.Assume;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;


/**
 *
 * @author dominik.stadler
 */
public class UrlUtilsTest {
    private static final Logger log = LoggerFactory.make();

	@After
	public void tearDown() throws InterruptedException {
		ThreadTestHelper.waitForThreadToFinishSubstring("NanoHTTP");
	}

    @Test
	public void testRetrieveDataString() throws Exception {
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t")) {
			assertEquals("expected\n\r\t",
					UrlUtils.retrieveData("http://localhost:" + server.getPort(), 0));
		}
	}

    @Test
	public void testRetrieveDataStringSSLFactory() throws Exception {
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t")) {
			assertEquals("expected\n\r\t",
					UrlUtils.retrieveData("http://localhost:" + server.getPort(), null, 0, HttpsURLConnection.getDefaultSSLSocketFactory()));
		}
	}

	@Test
	public void testRetrieveDataStringCreated() throws Exception {
		try (MockRESTServer server = new MockRESTServer("201 CREATED", NanoHTTPD.MIME_HTML, "expected")) {
			assertEquals("expected",
					UrlUtils.retrieveData("http://localhost:" + server.getPort(), 0));
		}
	}

	@Test
	public void testRetrieveDataStringAccepted() throws Exception {
		try (MockRESTServer server = new MockRESTServer("202 ACCEPTED", NanoHTTPD.MIME_HTML, "expected")) {
			assertEquals("expected",
					UrlUtils.retrieveData("http://localhost:" + server.getPort(), 0));
		}
	}

	@Test
	public void testRetrieveDataStringFailed() throws Exception {
		try (MockRESTServer server = new MockRESTServer("404 NOT FOUND", NanoHTTPD.MIME_HTML, "expected")) {
		    try {
    			UrlUtils.retrieveData("http://localhost:" + server.getPort(), 0);
    			fail("Should catch IOException with connection error information here");
    		} catch (IOException e) {
    			TestHelpers.assertContains(e, "404", "http://localhost:" + server.getPort()); // error code and url are mentioned in
    // the error message
    		}
	    }
	}

	@Test
	public void testRetrieveDataStringTimeout() throws Exception {
		try (MockRESTServer server = new MockRESTServer("201 CREATED", NanoHTTPD.MIME_HTML, "expected")) {
			assertEquals("expected",
					UrlUtils.retrieveData("http://localhost:" + server.getPort(), 10000));
		}
	}

	@Test
	public void testRetrieveRawData() throws Exception {
		try (MockRESTServer server = new MockRESTServer("201 CREATED", NanoHTTPD.MIME_HTML, "expected")) {
			assertArrayEquals("expected".getBytes(), UrlUtils.retrieveRawData("http://localhost:" + server.getPort(), 0));
		}
	}

	@Test
	public void testRetrieveRawDataInvalidHost() throws Exception {
		try {
			UrlUtils.retrieveRawData("http://localhost:19992", 0);
			fail("Expecting an exception here");
		} catch (@SuppressWarnings("unused") ConnectException e) {
			// expected
		}
	}

	@Test
	public void testRetrieveRawDataSSLFactory() throws Exception {
		try (MockRESTServer server = new MockRESTServer("201 CREATED", NanoHTTPD.MIME_HTML, "expected")) {
			assertArrayEquals("expected".getBytes(), UrlUtils.retrieveRawData("http://localhost:" + server.getPort(), 0, HttpsURLConnection.getDefaultSSLSocketFactory()));
		}
	}

	// The range of ports that we try to use for the listening.
	private static final int PORT_RANGE_START = 15100;
	private static final int PORT_RANGE_END = 15110;

	private static int getNextFreePort() throws IOException {
		for (int port = PORT_RANGE_START; port < PORT_RANGE_END; port++) {
			try {
				try (ServerSocket sock = new ServerSocket(port)) {
				    sock.close();
				}
				//
				return port;
			} catch (IOException e) {
				// seems to be taken, try next one
				log.warning("Port " + port + " seems to be used already, trying next one: " + e);
			}
		}

		throw new IOException("No free port found in the range of [" + PORT_RANGE_START + " - " + PORT_RANGE_END + "]");
	}

	@Test
	public void testRetrieveRawDataEmptyContentLength() throws Exception {
		// first try to get the next free port
		int port = getNextFreePort();

		NanoHTTPD httpd = new NanoHTTPD(port);

		try {
			try {
				UrlUtils.retrieveRawData("http://localhost:" + port + "/empty.txt", 0);
				fail("Should catch 404 error");
			} catch(IOException e) {
				TestHelpers.assertContains(e, "404");
			}

			assertArrayEquals("".getBytes(), UrlUtils.retrieveRawData("http://localhost:" + port + "/src/test/data/empty.txt", 0));
		} finally {
			httpd.stop();
		}
	}



    @Test
    public void testRetrievePostDataEmptyContentLength() throws Exception {
        // first try to get the next free port
        int port = getNextFreePort();

        NanoHTTPD httpd = new NanoHTTPD(port);

        try {
            try {
                UrlUtils.retrieveDataPost("http://localhost:" + port + "/empty.txt", null, "somevar:234\n\r", null, 1000);
                fail("Should catch 404 error");
            } catch(IOException e) {
                TestHelpers.assertContains(e, "404");
            }

            assertEquals("", UrlUtils.retrieveDataPost("http://localhost:" + port + "/src/test/data/empty.txt", null, "somevar:234\n\r", null, 1000));
        } finally {
            httpd.stop();
        }
    }

    @Test
	public void testRetrieveDataStringString() throws Exception {
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\u00F6\u00C4\u20AC")) {
		    NanoHTTPD.setEncoding("UTF-8");
			assertEquals("expected\u00F6\u00C4\u20AC",
					UrlUtils.retrieveData("http://localhost:" + server.getPort(), "UTF-8", 0));
		}
	}

	@Test
	public void testRetrieveDataStringStringEncoded() throws Exception {
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\u00F6\u00C4\u20AC")) {
		    NanoHTTPD.setEncoding("ISO-8859-15");
			assertEquals("expected\u00F6\u00C4\u20AC",
					UrlUtils.retrieveData("http://localhost:" + server.getPort(), "ISO-8859-15", 0));
		}
	}

	@Test
	public void testRetrieveDataStringStringInt() throws Exception {
		try (MockRESTServer server = new MockRESTServer(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "OK")) {
			UrlUtils.retrieveData("http://localhost:" + server.getPort(), null, 1000);
			fail("Should timeout here!");
		} catch (SocketTimeoutException e) {
			TestHelpers.assertContains(e, "Read timed out");
		}
	}

	@Test
	public void testIsAvailable() throws Exception {
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected")) {
			assertTrue(UrlUtils.isAvailable("http://localhost:" + server.getPort(), true, 0));
			assertTrue(UrlUtils.isAvailable("http://localhost:" + server.getPort(), false, 0));
			assertFalse(UrlUtils.isAvailable("http://notexistinghost:" + server.getPort(), true, 0));

			assertNull(UrlUtils.getAccessError("http://localhost:" + server.getPort(), true, false, 0));
			assertNull(UrlUtils.getAccessError("http://localhost:" + server.getPort(), false, false, 0));
			assertNotNull(UrlUtils.getAccessError("http://notexistinghost:" + server.getPort(), true, false, 0));

			assertNull(UrlUtils.getAccessError("http://localhost:" + server.getPort(), true, true, 0));
			assertNull(UrlUtils.getAccessError("http://localhost:" + server.getPort(), false, true, 0));
			assertNotNull(UrlUtils.getAccessError("http://notexistinghost:" + server.getPort(), true, true, 0));

			assertNull(UrlUtils.getAccessError("http://localhost:" + server.getPort(), true, false, 30000));
			assertNull(UrlUtils.getAccessError("http://localhost:" + server.getPort(), false, false, 30000));
			assertNotNull(UrlUtils.getAccessError("http://notexistinghost:" + server.getPort(), true, false, 30000));

			assertNotNull(UrlUtils.getAccessError("https://notexistinghost:" + server.getPort(), true, true, 30000));

			try {
				UrlUtils.getAccessError("https://notexistinghost:" + server.getPort(), true, true, -234);
				fail("Should catch exception here");
			} catch (IllegalArgumentException e) {
				TestHelpers.assertContains(e, "timeouts can't be negative");
			}

            try {
                UrlUtils.getAccessError("some://notexistinghost:" + server.getPort(), true, false, 0);
                fail("Should catch exception here");
            } catch (IllegalArgumentException e) {
                TestHelpers.assertContains(e, "Invalid destination URL");
            }
		}
	}

	@Test
	public void testIsAvailableSSLSocketFactory() throws Exception {
		try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected")) {
			assertTrue(UrlUtils.isAvailable("http://localhost:" + server.getPort(), true, true, 0, HttpsURLConnection.getDefaultSSLSocketFactory()));
			assertTrue(UrlUtils.isAvailable("http://localhost:" + server.getPort(), false, true, 0, HttpsURLConnection.getDefaultSSLSocketFactory()));
			assertFalse(UrlUtils.isAvailable("http://notexistinghost:" + server.getPort(), true, true, 0, HttpsURLConnection.getDefaultSSLSocketFactory()));

			assertNull(UrlUtils.getAccessError("http://localhost:" + server.getPort(), true, false, 0, HttpsURLConnection.getDefaultSSLSocketFactory()));
			assertNull(UrlUtils.getAccessError("http://localhost:" + server.getPort(), false, false, 0, HttpsURLConnection.getDefaultSSLSocketFactory()));
			assertNotNull(UrlUtils.getAccessError("http://notexistinghost:" + server.getPort(), true, false, 0, HttpsURLConnection.getDefaultSSLSocketFactory()));
		}
	}

	@Test
	public void testIsAvailableInvalidUrl() throws Exception {
		try {
			UrlUtils.isAvailable("invalidurl", true, 0);
			fail("Should catch exception because of invalid url here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Invalid destination URL");
		}

		try {
			UrlUtils.getAccessError("invalidurl", true, false, 30000);
			fail("Should catch exception because of invalid url here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Invalid destination URL");
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(UrlUtils.class);
	}

	@Test
	public void testRunWithDifferentLoglevel() {
		final AtomicReference<Exception> exception = new AtomicReference<>(null);
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testRetrieveDataString();
					testIsAvailable();
					testRetrieveDataPost();
				} catch (Exception e) {
					exception.set(e);
				}

			}
		}, UrlUtils.class.getName(), Level.FINE);

		assertNull(exception.get());
	}

    @Test
    public void testRetrieveDataPost() throws Exception {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t")) {
            try {
                UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), null, null, null, 1000);
                fail("Should fail with null-body");
            } catch (IllegalArgumentException e) {
                TestHelpers.assertContains(e, "POST request body must not be null");
            }

            try {
                UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), null, "", null, 1000);
                fail("Should timeout with empty body because of NanoHTTPD implementation details");
            } catch (@SuppressWarnings("unused") SocketTimeoutException e) {
				// expected here
            }

            try {
                UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), null, "", null, 0);
                fail("Should fail with 0 timeout");
            } catch (IllegalArgumentException e) {
                TestHelpers.assertContains(e, "Zero (infinite) timeouts not permitted");
            }

            try {
                UrlUtils.retrieveDataPost("http://invalidhost:" + server.getPort(), null, "some-var:234\n\r", null, 1000);
                fail("Should fail with invalid host");
            } catch (IOException e) {
                TestHelpers.assertContains(e, "invalidhost");
            }

            assertEquals("expected\n\r\t",
                    UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), null, "some-var:234\n\r", null, 1000));

            assertEquals("expected\n\r\t",
                    UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), "UTF-8", "some-var:234\n\r", null, 1000));

            assertEquals("expected\n\r\t",
                    UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), null, "some-var:234\n\r", "text/html", 1000));

            assertEquals("expected\n\r\t",
                    UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), null, "content-length:234\n\r", null, 1000));
        }
    }

    @Test
    public void testRetrieveDataPostSSLFactory() throws Exception {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "expected\n\r\t")) {
            assertEquals("expected\n\r\t",
                    UrlUtils.retrieveDataPost("http://localhost:" + server.getPort(), null, "content-length:234\n\r", null, 1000, HttpsURLConnection.getDefaultSSLSocketFactory()));
        }
    }

    @Test
    public void testSSLHostWithFacctory() throws IOException {
    	Assume.assumeTrue("Need access to https://www.google.com/ for this test to run",
    			UrlUtils.isAvailable("https://www.google.com/", false, 10000));

    	SSLSocketFactory sslFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
		assertTrue(UrlUtils.isAvailable("https://www.google.com/", true, false, 10000, sslFactory));
		assertNull(UrlUtils.getAccessError("https://www.google.com/", true, false, 10000, sslFactory));
		assertNotNull(UrlUtils.retrieveData("https://www.google.com/", null, 10000, sslFactory));

		// this will fail with 405 Method not allowed
		try {
			UrlUtils.retrieveDataPost("https://www.google.com/", null, "content-length:234\n\r", null, 10000, sslFactory);
		} catch (@SuppressWarnings("unused") IOException e) {
			// expected, url does not support POST method
		}
    }

    @Test
    public void testSSLHostNoFactory() throws IOException {
    	Assume.assumeTrue("Need access to https://www.google.com/ for this test to run",
    			UrlUtils.isAvailable("https://www.google.com/", false, 20000));

		assertTrue(UrlUtils.isAvailable("https://www.google.com/", true, false, 20000, null));
		assertNull(UrlUtils.getAccessError("https://www.google.com/", true, false, 20000, null));
		assertNotNull(UrlUtils.retrieveData("https://www.google.com/", null, 20000, null));

		// this will fail with 405 Method not allowed
		try {
			UrlUtils.retrieveDataPost("https://www.google.com/", null, "content-length:234\n\r", null, 10000, null);
		} catch (@SuppressWarnings("unused") IOException e) {
			// expected, url does not support POST method
		}
    }
}
