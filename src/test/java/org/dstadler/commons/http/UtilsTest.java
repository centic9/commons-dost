package org.dstadler.commons.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.dstadler.commons.net.SocketUtils;
import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.junit.Assume;
import org.junit.Test;

public class UtilsTest {

	private static final int NUMBER_OF_RUNS = 100000;

	/**
	 * Test method for {@link org.dstadler.commons.http.Utils#setSeed(long)}.
	 */
	@Test
	public void testSetSeed() {
		Utils.setSeed(234);
	}

	/**
	 * Test method for {@link org.dstadler.commons.http.Utils#getRandomURL()}.
	 */
	@Test
	public void testGetRandomURL() {
		Utils.setSeed(System.currentTimeMillis());

		for (int i = 0; i < NUMBER_OF_RUNS; i++) {
			assertNotNull(Utils.getRandomURL());
		}
	}

	/**
	 * Test method for {@link org.dstadler.commons.http.Utils#getRandomIP()}.
	 */
	@Test
	public void testGetRandomIP() {
		Utils.setSeed(System.currentTimeMillis());

		for (int i = 0; i < NUMBER_OF_RUNS; i++) {
			assertNotNull(Utils.getRandomIP());
		}
	}

	/**
	 * Test method for {@link org.dstadler.commons.http.Utils#isIgnorableException(java.lang.Exception)}.
	 */
	@Test
	public void testIsIgnorableException() {
		assertFalse(Utils.isIgnorableException(new Exception()));

		assertTrue(Utils.isIgnorableException(new NoRouteToHostException()));
		assertTrue(Utils.isIgnorableException(new SocketTimeoutException()));
		assertTrue(Utils.isIgnorableException(new Exception("Connection timed out")));
		assertTrue(Utils.isIgnorableException(new Exception("Network is unreachable")));
		assertTrue(Utils.isIgnorableException(new Exception("Connection refused")));
		assertTrue(Utils.isIgnorableException(new Exception("Server returned HTTP response code: 403")));
		assertTrue(Utils.isIgnorableException(new Exception("Server returned HTTP response code: 401")));
	}

	/**
	 * Test method for {@link org.dstadler.commons.http.Utils#getURL(java.lang.String, java.util.concurrent.atomic.AtomicInteger, long)}.
	 */
	@Test
	public void testGetURL() throws IOException {
	    Assume.assumeTrue("Could not access http://dstadler.org/",
	            checkURL(new URL("http://dstadler.org/")));

		assertTrue("Expect URL http://dstadler.org to work, but didn't",
				Utils.getURL("http://dstadler.org", new AtomicInteger(), 2));
		assertTrue("Expect URL http://dstadler.org to work, but didn't",
				Utils.getURL("http://dstadler.org", new AtomicInteger(99), 2));
		assertTrue("Expect URL http://dstadler.org to work, but didn't",
				Utils.getURL("http://dstadler.org", new AtomicInteger(100), 2));
		assertFalse("Expect URL http://dstadler.org to not work, but did",
				Utils.getURL("invalidurl", new AtomicInteger(100), 2));
		assertFalse("Expect URL http://dstadler.org to not work, but did",
				Utils.getURL("http://notexistingsomestrangeurlwhichshouldnotexist.com", new AtomicInteger(100), 2));
	}

	private boolean checkURL(URL url) {
		try {
			final URLConnection con = url.openConnection();
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);
			int read = con.getInputStream().read();
			assertTrue(read >= 0);
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	@Test
	public void testGetURLException() throws IOException {
		runWithFailureResponse(NanoHTTPD.HTTP_FORBIDDEN, true);
		runWithFailureResponse(NanoHTTPD.HTTP_NOTFOUND, true);
		runWithFailureResponse(NanoHTTPD.HTTP_NOTIMPLEMENTED, true);
		runWithFailureResponse(NanoHTTPD.HTTP_BADREQUEST, true);
		runWithFailureResponse(NanoHTTPD.HTTP_REDIRECT, false);
	}

	private void runWithFailureResponse(final String response, boolean fails) throws IOException {
		final int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port) {
			@Override
			public Response serve(String uri, String method, Properties header, Properties parms) {
				return new Response(response, NanoHTTPD.MIME_HTML, "<html>" +
						"error" +
						"</html>");
			}
		};

		try {
			assertEquals(!fails, Utils.getURL("http://localhost:" + port, new AtomicInteger(100), 2));
		} finally {
			httpd.stop();
		}
	}

	@Test
	public void testConnectionBreaks() throws IOException {
		final int port = SocketUtils.getNextFreePort(9000, 9010);
		NanoHTTPD httpd = new NanoHTTPD(port) {
			@Override
			public Response serve(String uri, String method, Properties header, Properties parms) {
				throw new IllegalStateException("Test exception");
			}
		};

		try {
			assertFalse(Utils.getURL("http://localhost:" + port, new AtomicInteger(100), 2));
		} finally {
			httpd.stop();
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(Utils.class);
	}
}
