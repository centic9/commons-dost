package org.dstadler.commons.http;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.dstadler.commons.net.UrlUtils;
import org.dstadler.commons.testing.MemoryLeakVerifier;
import org.dstadler.commons.testing.MockRESTServer;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This test is very similar to HttpClientWrapperTest so that we verify both
 * sync and async HttpClient in the same way
 */
@RunWith(Parameterized.class)
public class HttpAsyncClientWrapperTest {
    private static final MemoryLeakVerifier verifier = new MemoryLeakVerifier();

    private HttpAsyncClientWrapper wrapper;

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
            wrapper = new HttpAsyncClientWrapper("", null, 10000);
        } else {
            wrapper = new HttpAsyncClientWrapper(10000);
        }
    }

    @After
    public void tearDown() throws IOException {
        wrapper.close();

        verifier.assertGarbageCollected();
    }

    @Test
    public void testSimpleGet() throws Exception {
        assertNotNull(wrapper.getHttpClient());

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", wrapper.simpleGet("http://localhost:" + server.getPort()));
        }
    }

    @Test
    public void testSimpleGetWithBody() throws Exception {
        assertNotNull(wrapper.getHttpClient());

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", wrapper.simpleGet("http://localhost:" + server.getPort(), "some body data"));
        }

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", wrapper.simpleGet("http://localhost:" + server.getPort(), (String)null));
        }
    }

    @Test
    public void testSimpleGetStream() throws Exception {
        assertNotNull(wrapper.getHttpClient());

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            verifier.addObject(server);

            final AtomicReference<String> str = new AtomicReference<>();
            wrapper.simpleGet("http://localhost:" + server.getPort(), inputStream -> {
                verifier.addObject(inputStream);

                try {
                    str.set(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });

            assertEquals("ok", str.get());
        }
    }

    @Test
    public void testHttpAsyncClientWrapperNormalGet() throws Exception {
        assertNotNull(wrapper.getHttpClient());

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", wrapper.simpleGet("http://localhost:" + server.getPort()));

            final HttpGet httpGet = new HttpGet("http://localhost:" + server.getPort());
            HttpResponse response = wrapper.getHttpClient().execute(httpGet, null).get();
            HttpEntity entity = HttpAsyncClientWrapper.checkAndFetch(response, "http://localhost:" + server.getPort());

            // ensure none of the objects stays in memory after the client is closed
            verifier.addObject(httpGet);
            verifier.addObject(response);
            verifier.addObject(entity);
            verifier.addObject(entity.getContent());

            try {
                String string = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
                assertNotNull(string);
            } finally {
                // ensure all content is taken out to free resources
                EntityUtils.consume(entity);
            }
        }
    }

    @Test
    public void testHttpAsyncClientWrapperBytes() throws Exception {
        assertNotNull(wrapper.getHttpClient());

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertArrayEquals(new byte[] {111, 107}, wrapper.simpleGetBytes("http://localhost:" + server.getPort()));
        }
    }

    @Test
    public void testHttpAsyncClientWrapperHTTPS() throws Exception {
        Assume.assumeTrue("https://dstadler.org/ should be reachable", UrlUtils.isAvailable("https://dstadler.org/", false, 10_000));

        String ret = wrapper.simpleGet("https://dstadler.org/");
        assertNotNull(ret);
        assertTrue(ret.length() > 0);
    }

    @Test
    public void testHttpAsyncClientWrapperHTTPSBytes() throws Exception {
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
                HttpResponse response = wrapper.getHttpClient().execute(httpGet, null).get();
                verifier.addObject(httpGet);
                verifier.addObject(response);

                HttpAsyncClientWrapper.checkAndFetch(response, "http://localhost:" + server.getPort());
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
            assertEquals("ok", HttpAsyncClientWrapper.retrieveData("http://localhost:" + server.getPort()));
        }
    }

    @Test
    public void testRetrieveDataUser() throws IOException {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", HttpAsyncClientWrapper.retrieveData("http://localhost:" + server.getPort(), "", null, 10_000));
        }
    }

    @Test
    public void microBenchmark() throws IOException {
        // locally this executes in approx. 1 sec...
        for(int i = 0;i < 500;i++) {
            try (HttpAsyncClientWrapper client = new HttpAsyncClientWrapper("", null, 10_000)) {
                assertNotNull(client);
            }
        }
    }

    @Test
    public void testSimplePost() throws Exception {
        assertNotNull(wrapper.getHttpClient());

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", wrapper.simplePost("http://localhost:" + server.getPort(), "\n\r"));
        }

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", wrapper.simplePost("http://localhost:" + server.getPort(), "some body\n\r"));
        }

        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok")) {
            assertEquals("ok", wrapper.simplePost("http://localhost:" + server.getPort(), null));
            fail("Body 'null' currently fails because the MockRESTServer cannot handle it");
        } catch (SocketTimeoutException e) {
            // expected here
        }
    }

    @Test
    public void testDownloadFile() throws IOException {
        File tempFile = File.createTempFile("HttpAsyncClientWrapperDownloadFile", ".tst");
        assertTrue(tempFile.delete());
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/plain", "ok, file content")) {
            HttpAsyncClientWrapper.downloadFile("http://localhost:" + server.getPort(), tempFile, 60_000);

            assertTrue(tempFile.exists());
            assertEquals("ok, file content", FileUtils.readFileToString(tempFile, "UTF-8"));
        } finally {
            if(tempFile.exists()) {
                assertTrue(tempFile.delete());
            }
        }
    }

    @Ignore("Only for testing buffering when downloading to a file, but it simply did not have much effect")
    @Test
    public void testDownloadSpeed() throws IOException {
        long start = System.currentTimeMillis();
        for(int i = 0;i < 10;i++) {
            HttpAsyncClientWrapper.downloadFile("http://dstadler.org/example-debug.apk", new File("/tmp/example-debug.apk"), 60_000);
            System.out.println("Iteration " + i + ": " + (System.currentTimeMillis() - start));
        }

        System.out.println("Overall: " + (System.currentTimeMillis() - start) + ", avg: " + ((double)(System.currentTimeMillis() - start))/10);

        start = System.currentTimeMillis();
        for(int i = 0;i < 10;i++) {
            downloadWithBuffer("http://dstadler.org/example-debug.apk", new File("/tmp/example-debug.apk"), 60_000);
            System.out.println("Buffer: Iteration " + i + ": " + (System.currentTimeMillis() - start));
        }

        System.out.println("Buffer: Overall: " + (System.currentTimeMillis() - start) + ", avg: " + ((double)(System.currentTimeMillis() - start))/10);
    }

    private void downloadWithBuffer(String url, File destination, int timeoutMs) throws IOException, IllegalStateException {
        try (HttpAsyncClientWrapper client = new HttpAsyncClientWrapper(timeoutMs)) {
            client.simpleGet(url, inputStream -> {
                verifier.addObject(inputStream);

                try {
                    FileUtils.copyInputStreamToFile(new BufferedInputStream(inputStream, 100*1024), destination);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
        }
    }

    @Test(expected = IOException.class)
    public void testEmptyResponseEntity() throws Exception {
        try (MockRESTServer server = new MockRESTServer("404", "application/html", "")) {
            try (HttpAsyncClientWrapper httpClient = new HttpAsyncClientWrapper(10_000)) {
                String url = "http://localhost:" + server.getPort();
                final HttpUriRequest httpGet = new HttpHead(url);
                HttpResponse response = httpClient.getHttpClient().execute(httpGet, null).get();
                verifier.addObject(httpGet);
                verifier.addObject(response);

                assertNull("Entity is null in this case", response.getEntity());

                // this will throw an IOException, previously we caught a NullPointerException
                HttpAsyncClientWrapper.checkAndFetch(response, url);
            }
        }
    }
}
