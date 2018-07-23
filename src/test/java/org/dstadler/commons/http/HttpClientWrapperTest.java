package org.dstadler.commons.http;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.dstadler.commons.net.UrlUtils;
import org.dstadler.commons.testing.MockRESTServer;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class HttpClientWrapperTest {
    private HttpClientWrapper wrapper;

    @Parameterized.Parameters(name = "UseAuth: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { Boolean.TRUE }, { Boolean.FALSE },
        });
    }

    @Parameterized.Parameter
    public Boolean withAuth;

    @Before
    public void setUp() {
        if(withAuth) {
            wrapper = new HttpClientWrapper("", null, 10000);
        } else {
            wrapper = new HttpClientWrapper(10000);
        }
    }

    @After
    public void tearDown() throws IOException {
        wrapper.close();
    }

    @Test
    public void testHttpClientWrapperSimpleGet() throws Exception {
        assertNotNull(wrapper.getHttpClient());

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", wrapper.simpleGet("http://localhost:" + server.getPort()));
        }
    }

    @Test
    public void testHttpClientWrapperSimpleGetWithBody() throws Exception {
        assertNotNull(wrapper.getHttpClient());

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", wrapper.simpleGet("http://localhost:" + server.getPort(), "some body data"));
        }
    }

    @Test
    public void testHttpClientWrapperSimpleGetStream() throws Exception {
        assertNotNull(wrapper.getHttpClient());

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            final AtomicReference<String> str = new AtomicReference<>();
            wrapper.simpleGet("http://localhost:" + server.getPort(), inputStream -> {
                try {
                    str.set(IOUtils.toString(inputStream, "UTF-8"));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });

            assertEquals("ok", str.get());
        }
    }

    @Test
    public void testHttpClientWrapperNormalGet() throws Exception {
        assertNotNull(wrapper.getHttpClient());

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", wrapper.simpleGet("http://localhost:" + server.getPort()));

            final HttpGet httpGet = new HttpGet("http://localhost:" + server.getPort());
            try (CloseableHttpResponse response = wrapper.getHttpClient().execute(httpGet)) {
                HttpEntity entity = HttpClientWrapper.checkAndFetch(response, "http://localhost:" + server.getPort());

                try {
                    String string = IOUtils.toString(entity.getContent(), "UTF-8");
                    assertNotNull(string);
                } finally {
                    // ensure all content is taken out to free resources
                    EntityUtils.consume(entity);
                }
            }
        }
    }

    @Test
    public void testHttpClientWrapperBytes() throws Exception {
        assertNotNull(wrapper.getHttpClient());

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertArrayEquals(new byte[] {111, 107}, wrapper.simpleGetBytes("http://localhost:" + server.getPort()));
        }
    }

    @Test
    public void testHttpClientWrapperHTTPS() throws Exception {
        Assume.assumeTrue("https://dstadler.org/ should be reachable", UrlUtils.isAvailable("https://dstadler.org/", false, 10_000));

        String ret = wrapper.simpleGet("https://dstadler.org/");
        assertNotNull(ret);
        assertTrue(ret.length() > 0);
    }

    @Test
    public void testHttpClientWrapperHTTPSBytes() throws Exception {
        Assume.assumeTrue("https://dstadler.org/ should be reachable", UrlUtils.isAvailable("https://dstadler.org/", false, 10_000));

        byte[] ret = wrapper.simpleGetBytes("https://dstadler.org/");
        assertNotNull(ret);
        assertTrue(ret.length > 0);
    }

    @Test
    public void testSimpleGetFails() throws Exception {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_INTERNALERROR, "text/plain", "test error")) {
            try {
                wrapper.simpleGet("http://localhost:" + server.getPort());
                fail("Should throw an exception");
            } catch (IOException e) {
                TestHelpers.assertContains(e, "500", "test error");
            }
        }
    }

    @Test
    public void testCheckAndFetchFails() throws Exception {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_INTERNALERROR, "text/plain", "test error")) {
            try {
                final HttpGet httpGet = new HttpGet("http://localhost:" + server.getPort());
                try (CloseableHttpResponse response = wrapper.getHttpClient().execute(httpGet)) {
                    HttpClientWrapper.checkAndFetch(response, "http://localhost:" + server.getPort());
                }
                fail("Should throw an exception");
            } catch (IOException e) {
                TestHelpers.assertContains(e, "500", "test error");
            }
        }
    }

    @Test
    public void testSimpleGetBytesFails() throws Exception {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_INTERNALERROR, "text/plain", "test error")) {
            try {
                wrapper.simpleGetBytes("http://localhost:" + server.getPort());
                fail("Should throw an exception");
            } catch (IOException e) {
                TestHelpers.assertContains(e, "500", "test error");
            }
        }
    }

    @Test
    public void testSimpleGetException() throws Exception {
        try (MockRESTServer server = new MockRESTServer(() -> {
            throw new IllegalStateException("testexception");
        }, NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            try {
                wrapper.simpleGet("http://localhost:" + server.getPort());
                fail("Should throw an exception");
            } catch (IOException e) {
                TestHelpers.assertContains(e, "500", "testexception");
            }
        }
    }

    @Test
    public void testSimpleGetBytesException() throws Exception {
        try (MockRESTServer server = new MockRESTServer(() -> {
            throw new IllegalStateException("testexception");
        }, NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            try {
                wrapper.simpleGetBytes("http://localhost:" + server.getPort());
                fail("Should throw an exception");
            } catch (IOException e) {
                TestHelpers.assertContains(e, "500", "testexception");
            }
        }
    }

    @Test
    public void testRetrieveData() throws IOException {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", HttpClientWrapper.retrieveData("http://localhost:" + server.getPort()));
        }
    }

    @Test
    public void testRetrieveDataUser() throws IOException {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", HttpClientWrapper.retrieveData("http://localhost:" + server.getPort(), "", null, 10_000));
        }
    }

    @Test
    public void microBenchmark() throws IOException {
        // locally this executes in approx. 1 sec...
        for(int i = 0;i < 500;i++) {
            try (HttpClientWrapper client = new HttpClientWrapper("", null, 10_000)) {
                assertNotNull(client);
            }
        }
    }
}
