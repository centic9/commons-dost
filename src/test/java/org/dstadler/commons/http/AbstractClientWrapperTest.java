package org.dstadler.commons.http;

import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.input.NullInputStream;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractClientWrapperTest {
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { Boolean.TRUE }, { Boolean.FALSE },
        });
    }

    private AbstractClientWrapper wrapper;
    private final AtomicInteger simpleGetCount = new AtomicInteger();

    public void setUp(boolean withAuth) {
        wrapper = new AbstractClientWrapper(60_000, withAuth) {
            @Override
            protected void simpleGetInternal(String url, IOConsumer<InputStream> consumer, String body) throws IOException {
                simpleGetCount.incrementAndGet();
                consumer.accept(new NullInputStream());
            }

            @Override
            public void close() {
            }
        };
    }

    @AfterEach
    public void tearDown() throws IOException {
        wrapper.close();
    }

	@MethodSource("data")
	@ParameterizedTest(name = "UseAuth: {0}")
	public void testAbstract(Boolean withAuth) throws Exception {
		setUp(withAuth);
        assertNotNull(wrapper.createSSLContext());

        wrapper.simpleGet("url");
        assertEquals(1, simpleGetCount.get());

        wrapper.simpleGetBytes("url");
        assertEquals(2, simpleGetCount.get());

        wrapper.simpleGet("url", "body");
        assertEquals(3, simpleGetCount.get());

        wrapper.simpleGet("url", (String)null);
        assertEquals(4, simpleGetCount.get());

        AtomicBoolean called = new AtomicBoolean();
        wrapper.simpleGet("url", (x) -> called.set(true));
        assertEquals(5, simpleGetCount.get());
        assertTrue(called.get(), "Consumer should be call");
    }

	@MethodSource("data")
	@ParameterizedTest(name = "UseAuth: {0}")
	public void testHttpGet(Boolean withAuth) throws UnsupportedEncodingException {
		setUp(withAuth);
        HttpUriRequest httpGet = wrapper.getHttpGet("url", "body");
        assertNotNull(httpGet);
    }

	@MethodSource("data")
	@ParameterizedTest(name = "UseAuth: {0}")
	public void testHttpGetNullBody(Boolean withAuth) throws UnsupportedEncodingException {
		setUp(withAuth);
        HttpUriRequest httpGet = wrapper.getHttpGet("url", null);
        assertNotNull(httpGet);
    }

	@MethodSource("data")
	@ParameterizedTest(name = "UseAuth: {0}")
	public void testGetHttpHostWithAuth(Boolean withAuth) throws MalformedURLException {
		setUp(withAuth);
        HttpClientContext context = HttpClientContext.create();
        HttpHost host = wrapper.getHttpHostWithAuth("http://url", context);
        assertNotNull(host);
    }

	@MethodSource("data")
	@ParameterizedTest(name = "UseAuth: {0}")
	public void testWithIOException(Boolean withAuth) throws IOException {
		setUp(withAuth);
        try (AbstractClientWrapper wrapper = new AbstractClientWrapper(60_000, withAuth) {
            @Override
            protected void simpleGetInternal(String url, IOConsumer<InputStream> consumer, String body) throws IOException {
                simpleGetCount.incrementAndGet();
                consumer.accept(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw new IOException("test exception");
                    }
                });
            }

            @Override
            public void close() {
            }
        }) {

            assertThrows(IllegalStateException.class,
                    () -> wrapper.simpleGet("url"));
            assertThrows(IllegalStateException.class,
                    () -> wrapper.simpleGet("url", "body"));
            assertThrows(IllegalStateException.class,
                    () -> wrapper.simpleGet("url", (String) null));
            assertThrows(IllegalStateException.class,
                    () -> wrapper.simpleGetBytes("url"));

            wrapper.simpleGet("url", (x) -> {
            });
        }
    }
}
