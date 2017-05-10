package org.dstadler.commons.http;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.dstadler.commons.net.UrlUtils;
import org.dstadler.commons.testing.MockRESTServer;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.Assume;
import org.junit.Test;


public class HttpClientWrapperTest {

    @Test
    public void testHttpClientWrapperSimpleGet() throws Exception {
        try (HttpClientWrapper wrapper = new HttpClientWrapper("", null, 10000)) {
            assertNotNull(wrapper.getHttpClient());

            try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
                assertEquals("ok", wrapper.simpleGet("http://localhost:" + server.getPort()));
            }
        }
    }

    @Test
    public void testHttpClientWrapperSimpleGetStream() throws Exception {
        try (HttpClientWrapper wrapper = new HttpClientWrapper("", null, 10000)) {
            assertNotNull(wrapper.getHttpClient());

            try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
                final AtomicReference<String> str = new AtomicReference<>();
                wrapper.simpleGet("http://localhost:" + server.getPort(), new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) {
                        try {
                            str.set(IOUtils.toString(inputStream, "UTF-8"));
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                });

                assertEquals("ok", str.get());
            }
        }
    }

    @Test
    public void testHttpClientWrapperNormalGet() throws Exception {
        try (HttpClientWrapper wrapper = new HttpClientWrapper("", null, 10000)) {
            assertNotNull(wrapper.getHttpClient());

            try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
                assertEquals("ok", wrapper.simpleGet("http://localhost:" + server.getPort()));

                final HttpGet httpGet = new HttpGet("http://localhost:" + server.getPort());
                try (CloseableHttpResponse response = wrapper.getHttpClient().execute(httpGet)) {
                    HttpClientWrapper.checkAndFetch(response, "http://localhost:" + server.getPort());

                    HttpEntity entity = response.getEntity();

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
    }

    @Test
    public void testHttpClientWrapperBytes() throws Exception {
        try (HttpClientWrapper wrapper = new HttpClientWrapper("", null, 10000)) {
            assertNotNull(wrapper.getHttpClient());

            try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
                assertArrayEquals(new byte[] {111, 107}, wrapper.simpleGetBytes("http://localhost:" + server.getPort()));
            }
        }
    }

    @Test
    public void testHttpClientWrapperHTTPS() throws Exception {
        Assume.assumeTrue("https://www.google.com/ should be reachable", UrlUtils.isAvailable("https://www.google.com/", false, 10_000));

        try (HttpClientWrapper wrapper = new HttpClientWrapper("", null, 10000)) {
            String ret = wrapper.simpleGet("https://www.google.com/");
            assertNotNull(ret);
            assertTrue(ret.length() > 0);
        }
    }

    @Test
    public void testHttpClientWrapperHTTPSBytes() throws Exception {
        Assume.assumeTrue("https://www.google.com/ should be reachable", UrlUtils.isAvailable("https://www.google.com/", false, 10_000));

        try (HttpClientWrapper wrapper = new HttpClientWrapper("", null, 10000)) {
            byte[] ret = wrapper.simpleGetBytes("https://www.google.com/");
            assertNotNull(ret);
            assertTrue(ret.length > 0);
        }
    }

    @Test
    public void testSimpleGetFails() throws Exception {
        try (HttpClientWrapper wrapper = new HttpClientWrapper("", null, 10000)) {
            try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_INTERNALERROR, "text/plain", "ok")) {
                try {
                    wrapper.simpleGet("http://localhost:" + server.getPort());
                    fail("Should throw an exception");
                } catch (IOException e) {
                    TestHelpers.assertContains(e, "500");
                }
            }
        }
    }

    @Test
    public void testCheckAndFetchFails() throws Exception {
        try (HttpClientWrapper wrapper = new HttpClientWrapper("", null, 10000)) {
            try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_INTERNALERROR, "text/plain", "ok")) {
                try {
                    final HttpGet httpGet = new HttpGet("http://localhost:" + server.getPort());
                    try (CloseableHttpResponse response = wrapper.getHttpClient().execute(httpGet)) {
                        HttpClientWrapper.checkAndFetch(response, "http://localhost:" + server.getPort());
                    }
                    fail("Should throw an exception");
                } catch (IOException e) {
                    TestHelpers.assertContains(e, "500");
                }
            }
        }
    }

    @Test
    public void testSimpleGetBytesFails() throws Exception {
        try (HttpClientWrapper wrapper = new HttpClientWrapper("", null, 10000)) {
            try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_INTERNALERROR, "text/plain", "ok")) {
                try {
                    wrapper.simpleGetBytes("http://localhost:" + server.getPort());
                    fail("Should throw an exception");
                } catch (IOException e) {
                    TestHelpers.assertContains(e, "500");
                }
            }
        }
    }

    @Test
    public void testSimpleGetException() throws Exception {
        try (HttpClientWrapper wrapper = new HttpClientWrapper("", null, 10000)) {
            try (MockRESTServer server = new MockRESTServer(new Runnable() {
                @Override
                public void run() {
                    throw new IllegalStateException("testexception");
                }
            }, NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
                try {
                    wrapper.simpleGet("http://localhost:" + server.getPort());
                    fail("Should throw an exception");
                } catch (IOException e) {
                    TestHelpers.assertContains(e, "500");
                }
            }
        }
    }


    @Test
    public void testSimpleGetBytesException() throws Exception {
        try (HttpClientWrapper wrapper = new HttpClientWrapper("", null, 10000)) {
            try (MockRESTServer server = new MockRESTServer(new Runnable() {
                @Override
                public void run() {
                    throw new IllegalStateException("testexception");
                }
            }, NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
                try {
                    wrapper.simpleGetBytes("http://localhost:" + server.getPort());
                    fail("Should throw an exception");
                } catch (IOException e) {
                    TestHelpers.assertContains(e, "500");
                }
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
        // locally this executes in aprox. 1 sec...
        for(int i = 0;i < 500;i++) {
            try (HttpClientWrapper client = new HttpClientWrapper("", null, 10_000)) {
                assertNotNull(client);
            }
        }
    }
}
