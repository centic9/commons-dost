package org.dstadler.commons.metrics;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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
     * @param httpClient The HTTP Client that can be used to send metrics.
     *                   This can also contain credentials for basic authentication if necessary
     * @param url The base URL where Elasticsearch is available.
     *
     * @throws IOException If the HTTP call fails with an HTTP status code.
     */
    public static void sendMetric(String metric, int value, long ts, CloseableHttpClient httpClient, String url) throws IOException {
        final HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Content-Type", "application/json");
        httpPut.setEntity(new StringEntity(
                "{ \"timestamp\": " + ts + "," +
                        "  \"metric\": \"" + metric + "\"," +
                        "  \"value\": " + value + "}"));

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode > 201) {
                String msg = "Had HTTP StatusCode " + statusCode + " for request: " + url + ", response: " + response.getStatusLine().getReasonPhrase() + "\n" +
                        "Response: " + IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                log.warning(msg);

                throw new IOException(msg);
            }
            HttpEntity entity = response.getEntity();

            try {
                log.info("Had result when sending metric to Elasticsearch: " + IOUtils.toString(entity.getContent(), "UTF-8"));
            } finally {
                // ensure all content is taken out to free resources
                EntityUtils.consume(entity);
            }
        }
    }
}
