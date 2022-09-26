package org.dstadler.commons.http;

import org.apache.commons.io.input.NullInputStream;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class AbstractClientWrapperTest {
    @Parameterized.Parameters(name = "UseAuth: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { Boolean.TRUE }, { Boolean.FALSE },
        });
    }

    @Parameterized.Parameter
    public Boolean withAuth;

    private AbstractClientWrapper wrapper;
    private final AtomicInteger simpleGetCount = new AtomicInteger();

    @Before
    public void setUp() {
        wrapper = new AbstractClientWrapper(60_000, withAuth) {
            @Override
            protected void simpleGetInternal(String url, Consumer<InputStream> consumer, String body) {
                simpleGetCount.incrementAndGet();
                consumer.accept(new NullInputStream());
            }

            @Override
            public void close() {
            }
        };
    }

    @After
    public void tearDown() throws IOException {
        wrapper.close();
    }

    @Test
    public void testAbstract() throws Exception {
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
        assertTrue("Consumer should be call", called.get());
    }

    @Test
    public void testHttpGet() throws UnsupportedEncodingException {
        HttpUriRequest httpGet = wrapper.getHttpGet("url", "body");
        assertNotNull(httpGet);
    }

    @Test
    public void testHttpGetNullBody() throws UnsupportedEncodingException {
        HttpUriRequest httpGet = wrapper.getHttpGet("url", null);
        assertNotNull(httpGet);
    }

    @Test
    public void testGetHttpHostWithAuth() throws MalformedURLException {
        HttpClientContext context = HttpClientContext.create();
        HttpHost host = wrapper.getHttpHostWithAuth("http://url", context);
        assertNotNull(host);
    }

    @Test
    public void testWithIOException() throws IOException {
        try (AbstractClientWrapper wrapper = new AbstractClientWrapper(60_000, withAuth) {
            @Override
            protected void simpleGetInternal(String url, Consumer<InputStream> consumer, String body) {
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
