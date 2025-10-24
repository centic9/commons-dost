package org.dstadler.commons.http;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Helper class which configures a {@link HttpClient} instance
 * for usage with BasicAuthentication user/pwd and also
 * disables SSL verification to work with self-signed SSL certificates
 * on web pages.
 */
public class HttpClientWrapper extends AbstractClientWrapper implements Closeable {
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
	public HttpClientWrapper(String user, String password, int timeoutMs) {
		super(timeoutMs, true);

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
                new AuthScope(null, -1),
                new UsernamePasswordCredentials(user, password));

		RequestConfig reqConfig = RequestConfig.custom()
			    .setSocketTimeout(timeoutMs)
			    .setConnectTimeout(timeoutMs)
			    .setConnectionRequestTimeout(timeoutMs)
				// https://www.lenar.io/invalid-cookie-header-invalid-expires-attribute/
				.setCookieSpec(CookieSpecs.STANDARD)
			    .build();

		// configure the builder for HttpClients
		HttpClientBuilder builder = HttpClients.custom()
		        .setDefaultCredentialsProvider(credsProvider)
				.setDefaultRequestConfig(reqConfig);

		// add a permissive SSL Socket Factory to the builder
		createSSLSocketFactory(builder);

		// finally create the HttpClient instance
		this.httpClient = builder.build();
	}

    /**
     * Construct the {@link HttpClient} without using authentication values
     * and all timeouts set to the given number of milliseconds
     *
     * @param timeoutMs The timeout for socket connection and reading, specified in milliseconds
     */
	public HttpClientWrapper(int timeoutMs) {
		super(timeoutMs, false);

		RequestConfig reqConfig = RequestConfig.custom()
			    .setSocketTimeout(timeoutMs)
			    .setConnectTimeout(timeoutMs)
			    .setConnectionRequestTimeout(timeoutMs)
				// https://www.lenar.io/invalid-cookie-header-invalid-expires-attribute/
				.setCookieSpec(CookieSpecs.STANDARD)
			    .build();

		// configure the builder for HttpClients
		HttpClientBuilder builder = HttpClients.custom()
				.setDefaultRequestConfig(reqConfig);

		// add a permissive SSL Socket Factory to the builder
		createSSLSocketFactory(builder);

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
		final HttpUriRequest httpGet = getHttpGet(url, body);

		final CloseableHttpResponse execute;
        if(withAuth) {
			HttpClientContext context = HttpClientContext.create();
			HttpHost targetHost = getHttpHostWithAuth(url, context);
            execute = httpClient.execute(targetHost, httpGet, context);
        } else {
            execute = httpClient.execute(httpGet);
        }

        try (CloseableHttpResponse response = execute) {
			HttpEntity entity = checkAndFetch(response, url);
		    try {
				consumer.accept(entity.getContent());
		    } finally {
			    // ensure all content is taken out to free resources
			    EntityUtils.consume(entity);
		    }
		}
	}

	public String simplePost(String url, String body) throws IOException {
		HttpClientContext context = HttpClientContext.create();
		HttpHost targetHost = getHttpHostWithAuth(url, context);

		final HttpPost httpPost = new HttpPost(url);
		if(body != null) {
			httpPost.setEntity(new StringEntity(body));
		}
		try (CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context)) {
			HttpEntity entity = HttpClientWrapper.checkAndFetch(response, url);

			try {
				return IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
			} finally {
				// ensure all content is taken out to free resources
				EntityUtils.consume(entity);
			}
		}
	}

	private void createSSLSocketFactory(HttpClientBuilder builder) {
		try {
	        // Trust all certs, even self-signed and invalid hostnames, ...
	        final SSLContext sslcontext = createSSLContext();

	        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
	                sslcontext,
	                NoopHostnameVerifier.INSTANCE) {

						@Override
						protected void prepareSocket(SSLSocket socket) throws IOException {
							super.prepareSocket(socket);

							// workaround for https://issues.apache.org/jira/browse/HTTPCLIENT-1478
							socket.setSoTimeout(timeoutMs);

							// when running with Java 6 we should remove the outdated SSLv2Hello protocol as it is not
							// supported any more by dynaTrace after applying the FixPack for the POODLE SSLv3 attack
							Set<String> protocols = new HashSet<>(Arrays.asList(socket.getEnabledProtocols()));
							protocols.remove("SSLv2Hello");
							socket.setEnabledProtocols(protocols.toArray(new String[0]));
						}

	        };

			builder.setSSLSocketFactory(sslsf);
			//builder = builder.setHostnameVerifier(new AllowAllHostnameVerifier());
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
		try (HttpClientWrapper wrapper = new HttpClientWrapper(user, password, timeoutMs)) {
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
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode > 206) {
			String msg = "Had HTTP StatusCode " + statusCode + " for request: " + url + ", response: " +
					response.getStatusLine().getReasonPhrase() + "\n" +
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
		try (HttpClientWrapper client = new HttpClientWrapper(timeoutMs)) {
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
