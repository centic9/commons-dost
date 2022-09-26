package org.dstadler.commons.http;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpAsyncClientWrapper extends AbstractClientWrapper implements Closeable {
    private final static Logger log = LoggerFactory.make();

    private final CloseableHttpAsyncClient httpClient;

    /**
     * Construct the {@link HttpClient} with the given authentication values
     * and all timeouts set to the given number of milliseconds
     *
     * @param user The username for basic authentication, use an empty string when no authentication is required
     * @param password The password for basic authentication, null when no authentication is required
     * @param timeoutMs The timeout for socket connection and reading, specified in milliseconds
     */
    public HttpAsyncClientWrapper(String user, String password, int timeoutMs) {
        super(timeoutMs, true);

        RequestConfig reqConfig = RequestConfig.custom()
                //.setSocketTimeout(timeoutMs)
                .setConnectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .setConnectionRequestTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .build();

        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(null, -1),
                new UsernamePasswordCredentials(user, password.toCharArray()));

        HttpAsyncClientBuilder builder = HttpAsyncClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultRequestConfig(reqConfig);

        try {
            // create permissive ssl context
            final SSLContext sslcontext = createSSLContext();
            // TODO: builder.setSSLContext(sslcontext);
        } catch (GeneralSecurityException e) {
            log.log(Level.WARNING, "Could not create SSLSocketFactory for accepting all certificates", e);
        }

        // finally create the HttpClient instance and start it
        this.httpClient = builder.build();
        httpClient.start();
    }

    /**
     * Construct the {@link HttpClient} without using authentication values
     * and all timeouts set to the given number of milliseconds
     *
     * @param timeoutMs The timeout for socket connection and reading, specified in milliseconds
     */
    public HttpAsyncClientWrapper(int timeoutMs) {
        super(timeoutMs, false);


        RequestConfig reqConfig = RequestConfig.custom()
                //.setSocketTimeout(timeoutMs)
                .setConnectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .setConnectionRequestTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .build();

        HttpAsyncClientBuilder builder = HttpAsyncClients.custom()
                .setDefaultRequestConfig(reqConfig);

        try {
            // create permissive ssl context
            final SSLContext sslcontext = createSSLContext();
            // TODO: builder.setSSLContext(sslcontext);
        } catch (GeneralSecurityException e) {
            log.log(Level.WARNING, "Could not create SSLSocketFactory for accepting all certificates", e);
        }

        // finally create the HttpClient instance and start it
        this.httpClient = builder.build();
        httpClient.start();
    }

    /**
     * Return the current {@link HttpClient} instance.
     *
     * @return The internally used instance of the {@link HttpClient}
     */
    public CloseableHttpAsyncClient getHttpClient() {
        return httpClient;
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    protected void simpleGetInternal(String url, Consumer<InputStream> consumer, String body) throws IOException {
        final HttpUriRequest httpGet = getHttpGet(url, body);

        final HttpResponse execute;
        try {
            if (withAuth) {
                HttpClientContext context = HttpClientContext.create();
                HttpHost targetHost = getHttpHostWithAuth(url, context);

                execute = httpClient.execute(targetHost, httpGet, context, null).get();
            } else {
                execute = httpClient.execute(httpGet, null).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException)e.getCause();
            }

            throw new IOException(e);
        }

        HttpEntity entity = checkAndFetch(execute, url);
        try {
            consumer.accept(entity.getContent());
        } finally {
            // ensure all content is taken out to free resources
            EntityUtils.consume(entity);
        }
    }

    public String simplePost(String url, String body) throws IOException {
        HttpClientContext context = HttpClientContext.create();
        HttpHost targetHost = getHttpHostWithAuth(url, context);

        final HttpPost httpPost = new HttpPost(url);
        if(body != null) {
            httpPost.setEntity(new StringEntity(body));
        }

        try {
            HttpResponse response = httpClient.execute(targetHost, httpPost, context, null).get();
            HttpEntity entity = HttpClientWrapper.checkAndFetch(response, url);

            try {
                return IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
            } finally {
                // ensure all content is taken out to free resources
                EntityUtils.consume(entity);
            }
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException)e.getCause();
            }

            throw new IOException(e);
        }
    }

    /**
     * Small helper method to simply query the URL without password and
     * return the resulting data.
     *
     * @param url The URL to query data from.
     * @return The resulting data read from the URL
     * @throws IOException If the URL is not accessible or the query returns
     * 		a HTTP code other than 200.
     */
    public static String retrieveData(String url) throws IOException {
        return retrieveData(url, "", null, 10_000);
    }

    /**
     * Small helper method to simply query the URL without password and
     * return the resulting data.
     *
     * @param url The URL to query data from.
     * @param user The username to send
     * @param password The password to send
     * @param timeoutMs How long in milliseconds to wait for the request
     * @return The resulting data read from the URL
     * @throws IOException If the URL is not accessible or the query returns
     * 		a HTTP code other than 200.
     */
    public static String retrieveData(String url, String user, String password, int timeoutMs) throws IOException {
        try (HttpAsyncClientWrapper wrapper = new HttpAsyncClientWrapper(user, password, timeoutMs)) {
            return wrapper.simpleGet(url);
        }
    }

    /**
     * Helper method to check the status code of the response and throw an IOException if it is
     * an error or moved state.
     *
     * @param response A HttpResponse that is resulting from executing a HttpMethod.
     * @param url The url, only used for building the error message of the exception.
     *
     * @return The {@link HttpEntity} returned from response.getEntity().
     *
     * @throws IOException if the HTTP status code is higher than 206.
     */
    public static HttpEntity checkAndFetch(HttpResponse response, String url) throws IOException {
        // simply re-use the implementation from the original HttpClientWrapper for now
        return HttpClientWrapper.checkAndFetch(response, url);
    }

    /**
     * Download the data from the given URL to a local file.
     *
     * Copying is done incrementally to allow to download large files
     * without exceed memory.
     *
     * Creates directories if necessary.
     *
     * @param url The URL to download
     * @param destination The destination for the file
     * @param timeoutMs Socket/HTTP-timeout in milliseconds
     *
     * @throws IOException If accessing the URL or downloading data fails
     * @throws IllegalStateException If writing the file fails
     */
    public static void downloadFile(String url, File destination, int timeoutMs) throws IOException, IllegalStateException {
        // simply re-use the implementation from the original HttpClientWrapper for now
        HttpClientWrapper.downloadFile(url, destination, timeoutMs);
    }
}
