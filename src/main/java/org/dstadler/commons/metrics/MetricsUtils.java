package org.dstadler.commons.metrics;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.dstadler.commons.http.HttpClientWrapper;
import org.dstadler.commons.http.NanoHTTPD;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Helper class for sending simple metrics to an Elasticsearch instance.
 */
public class MetricsUtils {
    private static final Logger log = LoggerFactory.make();

    /**
     * Send the given value for the given metric and timestamp.
     *
     * Authentication can be provided via the configured {@link HttpClient} instance.
     *
     * @param metric The key of the metric
     * @param value The value of the measurement
     * @param ts The timestamp of the measurement
     * @param url The base URL where Elasticsearch is available.
     * @param user The username for basic authentication of the HTTP connection, empty if unused
     * @param password The password for basic authentication of the HTTP connection, null if unused
     *
     * @throws IOException If the HTTP call fails with an HTTP status code.
     */
    public static void sendMetric(String metric, int value, long ts, String url, String user, String password) throws IOException {
        try (HttpClientWrapper metrics = new HttpClientWrapper(user, password, 60_000)) {
            sendMetric(metric, value, ts, metrics.getHttpClient(), url);
        }
    }

    /**
     * Send the given value for the given metric and timestamp.
     *
     * Authentication can be provided via the configured {@link HttpClient} instance.
     *
     * @param metric The key of the metric
     * @param value The value of the measurement
     * @param ts The timestamp of the measurement
     * @param httpClient The HTTP Client that can be used to send metrics.
     *                   This can also contain credentials for basic authentication if necessary
     * @param url The base URL where Elasticsearch is available.
     *
     * @throws IOException If the HTTP call fails with an HTTP status code.
     */
    public static void sendMetric(String metric, int value, long ts, CloseableHttpClient httpClient, String url) throws IOException {
        sendMetric(null, metric, value, ts, httpClient, url);
    }

    /**
     * Send the given value for the given metric and timestamp.
     *
     * Authentication can be provided via the configured {@link HttpClient} instance.
     *
     * @param splitting Allows to define multiple values for one metric at one point in time, e.g. by machine, ...
     *                  Can be null if no splitting should be set
     * @param metric The key of the metric
     * @param value The value of the measurement
     * @param ts The timestamp of the measurement
     * @param httpClient The HTTP Client that can be used to send metrics.
     *                   This can also contain credentials for basic authentication if necessary
     * @param url The base URL where Elasticsearch is available.
     *
     * @throws IOException If the HTTP call fails with an HTTP status code.
     */
    public static void sendMetric(String splitting, String metric, int value, long ts,
                                  CloseableHttpClient httpClient, String url) throws IOException {
        sendDocument("{ \"timestamp\": " + ts + "," +
                        (splitting != null ? "  \"splitting\": \"" + splitting + "\"," : "") +
                        "  \"metric\": \"" + metric + "\"," +
                        "  \"value\": " + value + "}",
                httpClient, url);
    }

    /**
     * Send the given document to the given Elasticsearch URL/Index
     *
     * Authentication can be provided via the configured {@link HttpClient} instance.
     *
     * @param json The json-string to store as document
     * @param httpClient The HTTP Client that can be used to send metrics.
     *                   This can also contain credentials for basic authentication if necessary
     * @param url The base URL where Elasticsearch is available.
     *
     * @throws IOException If the HTTP call fails with an HTTP status code.
     */
    public static void sendDocument(String json, CloseableHttpClient httpClient, String url) throws IOException {
        final HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Content-Type", NanoHTTPD.MIME_JSON);
        httpPut.setEntity(new StringEntity(
                json, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            HttpEntity entity = HttpClientWrapper.checkAndFetch(response, url);

            try {
                log.info("Had result when sending document to Elasticsearch at " + url + ": " + IOUtils.toString(entity.getContent(), "UTF-8"));
            } finally {
                // ensure all content is taken out to free resources
                EntityUtils.consume(entity);
            }
        }
    }
}
