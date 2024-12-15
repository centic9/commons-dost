package org.dstadler.commons.metrics;

import org.dstadler.commons.http.HttpClientWrapper;
import org.dstadler.commons.testing.MockRESTServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MetricsUtilsTest {
    @Test
    public void testSendMetricURL() throws Exception {
        try (MockRESTServer server = new MockRESTServer("200", "application/json", "OK")) {
            String url = "http://localhost:" + server.getPort();
            MetricsUtils.sendMetric("testmetric", 123, System.currentTimeMillis(), url, "", null);
        }
    }

    @Test
    public void testSendMetric() throws Exception {
        try (MockRESTServer server = new MockRESTServer("200", "application/json", "OK");
                HttpClientWrapper metrics = new HttpClientWrapper("", null, 60_000)) {
            String url = "http://localhost:" + server.getPort();
            MetricsUtils.sendMetric("testmetric", 123, System.currentTimeMillis(), metrics.getHttpClient(), url);
        }
    }

    @Test
    public void testSendMetricWithSplitting() throws Exception {
        try (MockRESTServer server = new MockRESTServer("200", "application/json", "OK");
                HttpClientWrapper metrics = new HttpClientWrapper("", null, 60_000)) {
            String url = "http://localhost:" + server.getPort();
            MetricsUtils.sendMetric("splitting", "testmetric", 123, System.currentTimeMillis(), metrics.getHttpClient(), url);
        }
    }

    @Test
    public void testSendDocument() throws Exception {
        try (MockRESTServer server = new MockRESTServer("200", "application/json", "OK");
             HttpClientWrapper metrics = new HttpClientWrapper("", null, 60_000)) {
            String url = "http://localhost:" + server.getPort();
            MetricsUtils.sendDocument("{ \"test\": \"value\"}", metrics.getHttpClient(), url);
        }
    }

    @Test
    public void testSendMetricFails() {
		assertThrows(IOException.class, () -> {
			try (MockRESTServer server = new MockRESTServer("503", "application/json", "ERROR");
                HttpClientWrapper metrics = new HttpClientWrapper("", null, 60_000)) {
				String url = "http://localhost:" + server.getPort();
				MetricsUtils.sendMetric("testmetric", 123, System.currentTimeMillis(), metrics.getHttpClient(), url);
			}
		});
	}

    // helper method to get coverage of the unused constructor
    @Test
    public void testPrivateConstructor() throws Exception {
        org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(MetricsUtils.class);
    }
}
