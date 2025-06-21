package org.dstadler.commons.http5;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class AbstractClientWrapper5 implements Closeable {
    protected final int timeoutMs;
    protected final boolean withAuth;

    public AbstractClientWrapper5(int timeoutMs, boolean withAuth) {
        this.timeoutMs = timeoutMs;
        this.withAuth = withAuth;
    }

    protected SSLContext createSSLContext() throws GeneralSecurityException {
        // Trust all certs, even self-signed and invalid hostnames, ...
        final SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null,
                new TrustManager[] { new X509TrustManager() {

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(
                            X509Certificate[] certs, String authType) {
                        //
                    }

                    @Override
                    public void checkServerTrusted(
                            X509Certificate[] certs, String authType) {
                        //
                    }
                }
                }, new SecureRandom());
        return sslcontext;
    }

    /**
     * Perform a simple get-operation and return the resulting String.
     *
     * @param url The URL to query
     * @return The data returned when retrieving the data from the given url, converted to a String.
     * @throws IOException if the HTTP status code is not 200.
     */
    public String simpleGet(String url) throws IOException {
        final AtomicReference<String> str = new AtomicReference<>();
        simpleGetInternal(url, inputStream -> {
            try {
                str.set(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }, null);

        return str.get();
    }

    /**
     * Perform a simple get-operation with a request body and return the resulting String.
     *
     * @param url The URL to query
     * @param body Additional data to send with the request as HTTP body
     * @return The data returned when retrieving the data from the given url, converted to a String.
     * @throws IOException if the HTTP status code is not 200.
     */
    public String simpleGet(String url, String body) throws IOException {
        final AtomicReference<String> str = new AtomicReference<>();
        simpleGetInternal(url, inputStream -> {
            try {
                str.set(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }, body);

        return str.get();
    }

    /**
     * Perform a simple get-operation and return the resulting byte-array.
     *
     * @param url The URL to query
     * @return The data returned when retrieving the data from the given url.
     * @throws IOException if the HTTP status code is not 200.
     */
    public byte[] simpleGetBytes(String url) throws IOException {
        final AtomicReference<byte[]> bytes = new AtomicReference<>();
        simpleGetInternal(url, inputStream -> {
            try {
                bytes.set(IOUtils.toByteArray(inputStream));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }, null);

        return bytes.get();
    }

    /**
     * Perform a simple get-operation and passes the resulting InputStream to the given Consumer
     *
     * @param url The URL to query
     * @param consumer A Consumer which receives the InputStream and can process the data
     *                 on-the-fly in streaming fashion without retrieving all of the data into memory
     *                 at once.
     *
     * @throws IOException if the HTTP status code is not 200.
     */
    public void simpleGet(String url, Consumer<InputStream> consumer) throws IOException {
        simpleGetInternal(url, consumer, null);
    }

    protected HttpRequest getHttpGet(String url, String body) throws UnsupportedEncodingException {
        final BasicClassicHttpRequest httpGet;
        if(body == null) {
            httpGet = new HttpGet(url);
        } else {
            httpGet = new HttpGetWithBody5(url);
            httpGet.setEntity(new StringEntity(body));
        }
        return httpGet;
    }

    protected abstract void simpleGetInternal(String url, Consumer<InputStream> consumer, String body) throws IOException;

    protected HttpHost getHttpHostWithAuth(String url, HttpClientContext context) throws MalformedURLException {
        // Required to avoid two requests instead of one: See http://stackoverflow.com/questions/20914311/httpclientbuilder-basic-auth
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();

        // Generate BASIC scheme object and add it to the local auth cache
        URL cacheUrl = new URL(url);
        HttpHost targetHost = new HttpHost(cacheUrl.getProtocol(), cacheUrl.getHost(), cacheUrl.getPort());
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        //context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        return targetHost;
    }
}
