package org.dstadler.commons.http5;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.client.config.CookieSpecs;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Helper class which configures a {@link HttpClient} instance
 * for usage with BasicAuthentication user/pwd and also
 * disables SSL verification to work with self-signed SSL certificates
 * on web pages.
 */
public class HttpClientWrapper5 extends AbstractClientWrapper5 implements Closeable {
	private final static Logger log = LoggerFactory.make();

	private final CloseableHttpClient httpClient;

	/**
	 * Construct the {@link HttpClient} with the given authentication values
	 * and all timeouts set to the given number of milliseconds
	 *
	 * @param user The username for basic authentication, use an empty string when no authentication is required
	 * @param password The password for basic authentication, null when no authentication is required
	 * @param timeoutMs The timeout for socket connection and reading, specified in milliseconds
	 */
	public HttpClientWrapper5(String user, String password, int timeoutMs) {
		this(user, password, timeoutMs, false);
	}
	/**
	 * Construct the {@link HttpClient} with the given authentication values
	 * and all timeouts set to the given number of milliseconds
	 *
	 * @param user The username for basic authentication, use an empty string when no authentication is required
	 * @param password The password for basic authentication, null when no authentication is required
	 * @param timeoutMs The timeout for socket connection and reading, specified in milliseconds
	 */
	public HttpClientWrapper5(String user, String password, int timeoutMs, boolean allowAll) {
		super(timeoutMs, true);

		BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
                new AuthScope(null, -1),
                new UsernamePasswordCredentials(user, password == null ? null : password.toCharArray()));

		RequestConfig reqConfig = RequestConfig.custom()
			    .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeoutMs))
				// https://www.lenar.io/invalid-cookie-header-invalid-expires-attribute/
				.setCookieSpec(CookieSpecs.STANDARD)
			    .build();

		// configure the builder for HttpClients
		HttpClientBuilder builder = HttpClients.custom()
		        .setDefaultCredentialsProvider(credsProvider)
				.setDefaultRequestConfig(reqConfig);

		// add a permissive SSL Socket Factory to the builder
		createSSLSocketFactory(builder, allowAll);

		// finally create the HttpClient instance
		this.httpClient = builder.build();
	}

    /**
     * Construct the {@link HttpClient} without using authentication values
     * and all timeouts set to the given number of milliseconds
     *
     * @param timeoutMs The timeout for socket connection and reading, specified in milliseconds
     */
	public HttpClientWrapper5(int timeoutMs) {
		this(timeoutMs, false);
	}

    /**
     * Construct the {@link HttpClient} without using authentication values
     * and all timeouts set to the given number of milliseconds
     *
     * @param timeoutMs The timeout for socket connection and reading, specified in milliseconds
     */
	public HttpClientWrapper5(int timeoutMs, boolean allowAll) {
		super(timeoutMs, false);

		RequestConfig reqConfig = RequestConfig.custom()
			    .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeoutMs))
				// https://www.lenar.io/invalid-cookie-header-invalid-expires-attribute/
				.setCookieSpec(CookieSpecs.STANDARD)
			    .build();

		// configure the builder for HttpClients
		HttpClientBuilder builder = HttpClients.custom()
				.setDefaultRequestConfig(reqConfig);

		// add a permissive SSL Socket Factory to the builder
		createSSLSocketFactory(builder, allowAll);

		// finally create the HttpClient instance
		this.httpClient = builder.build();
	}

	/**
	 * Return the current {@link HttpClient} instance.
	 *
	 * @return The internally used instance of the {@link HttpClient}
	 */
	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	protected void simpleGetInternal(String url, IOConsumer<InputStream> consumer, String body) throws IOException {
		final ClassicHttpRequest httpGet = getHttpGet(url, body);

		httpClient.execute(httpGet, response -> {
			HttpEntity entity = checkAndFetch(response, url);
			try {
				consumer.accept(entity.getContent());
			} finally {
				// ensure all content is taken out to free resources
				EntityUtils.consume(entity);
			}

			return null;
		});
	}

	public String simplePost(String url, String body) throws IOException {
		//HttpClientContext context = HttpClientContext.create();
		//HttpHost targetHost = getHttpHostWithAuth(url, context);

		final HttpPost httpPost = new HttpPost(url);
		if(body != null) {
			httpPost.setEntity(new StringEntity(body));
		}

		return httpClient.execute(httpPost, response -> {
			HttpEntity entity = HttpClientWrapper5.checkAndFetch(response, url);

			try {
				return IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
			} finally {
				// ensure all content is taken out to free resources
				EntityUtils.consume(entity);
			}
		});
	}

	private void createSSLSocketFactory(HttpClientBuilder builder, boolean allowAll) {
		try {
			PoolingHttpClientConnectionManagerBuilder connMgrBuilder = PoolingHttpClientConnectionManagerBuilder.create()
					.setTlsSocketStrategy(ClientTlsStrategyBuilder.create()
							.setSslContext(SSLContexts.createSystemDefault())
							.setTlsVersions(TLS.V_1_2, TLS.V_1_3)
							.buildClassic())
					.setDefaultSocketConfig(SocketConfig.custom()
							.setSoTimeout(Timeout.ofMilliseconds(timeoutMs))
							.build())
					.setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
					.setConnPoolPolicy(PoolReusePolicy.LIFO)
					.setDefaultConnectionConfig(ConnectionConfig.custom()
							.setSocketTimeout(Timeout.ofMilliseconds(timeoutMs))
							.setConnectTimeout(Timeout.ofMilliseconds(timeoutMs))
							.setTimeToLive(TimeValue.ofMinutes(10))
							.build());

			if (allowAll) {
				// Trust all certs, even self-signed and invalid hostnames, ...
				connMgrBuilder.setSSLSocketFactory(
					SSLConnectionSocketFactoryBuilder.create()
							.setSslContext(SSLContextBuilder.create()
									.loadTrustMaterial(TrustAllStrategy.INSTANCE)
									.build())
							.setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
							.build());
			}

			PoolingHttpClientConnectionManager connectionManager = connMgrBuilder
					.build();

			builder.setConnectionManager(connectionManager);

		} catch (GeneralSecurityException e) {
			log.log(Level.WARNING, "Could not create SSLSocketFactory for accepting all certificates", e);
		}
	}

	@Override
	public void close() throws IOException {
		httpClient.close();
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
		try (HttpClientWrapper5 wrapper = new HttpClientWrapper5(user, password, timeoutMs)) {
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
    public static HttpEntity checkAndFetch(ClassicHttpResponse response, String url) throws IOException {
        int statusCode = response.getCode();
        if(statusCode > 206) {
			String msg = "Had HTTP StatusCode " + statusCode + " for request: " + url + ", response: " +
					response.getReasonPhrase() + "\n" +
					(response.getFirstHeader("Location") == null ? "" : response.getFirstHeader("Location") + "\n") +
					(response.getEntity() == null ? "" : StringUtils.abbreviate(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8), 1024));
            log.warning(msg);

            throw new IOException(msg);
        }

        return response.getEntity();
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
		log.info("Downloading from " + url + " to " + destination);
		try (HttpClientWrapper5 client = new HttpClientWrapper5(timeoutMs)) {
			client.simpleGet(url, inputStream -> {
				try {
					FileUtils.copyInputStreamToFile(inputStream, destination);
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			});
		}
	}
}
