package org.dstadler.commons.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dstadler.commons.logging.jdk.LoggerFactory;


/**
 * Helper class which configures a {@link HttpClient} instance
 * for usage with BasicAuthentication user/pwd and also
 * disables SSL verification to work with self-signed SSL certificates
 * on web pages.
 */
public class HttpClientWrapper implements Closeable {
	private final static Logger log = LoggerFactory.make();

	private final CloseableHttpClient httpClient;

	private final int timeoutMs;
	private final boolean withAuth;

	/**
	 * Construct the {@link HttpClient} with the given authentication values
	 * and all timeouts set to the given number of milliseconds
	 *
	 * @param user The username for basic authentication, use an empty string when no authentication is required
	 * @param password The password for basic authentication, null when no authentication is required
	 * @param timeoutMs The timeout for socket connection and reading, specified in milliseconds
	 */
	public HttpClientWrapper(String user, String password, int timeoutMs) {
		super();

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
                new AuthScope(null, -1),
                new UsernamePasswordCredentials(user, password));

		RequestConfig reqConfig = RequestConfig.custom()
			    .setSocketTimeout(timeoutMs)
			    .setConnectTimeout(timeoutMs)
			    .setConnectionRequestTimeout(timeoutMs)
			    .build();

		// configure the builder for HttpClients
		HttpClientBuilder builder = HttpClients.custom()
		        .setDefaultCredentialsProvider(credsProvider)
				.setDefaultRequestConfig(reqConfig);

		// add a permissive SSL Socket Factory to the builder
		builder = createSSLSocketFactory(builder);

		// finally create the HttpClient instance
		this.httpClient = builder.build();
		this.timeoutMs = timeoutMs;
		this.withAuth = true;
	}

    /**
     * Construct the {@link HttpClient} without using authentication values
     * and all timeouts set to the given number of milliseconds
     *
     * @param timeoutMs The timeout for socket connection and reading, specified in milliseconds
     */
	public HttpClientWrapper(int timeoutMs) {
		super();

		RequestConfig reqConfig = RequestConfig.custom()
			    .setSocketTimeout(timeoutMs)
			    .setConnectTimeout(timeoutMs)
			    .setConnectionRequestTimeout(timeoutMs)
			    .build();

		// configure the builder for HttpClients
		HttpClientBuilder builder = HttpClients.custom()
				.setDefaultRequestConfig(reqConfig);

		// add a permissive SSL Socket Factory to the builder
		builder = createSSLSocketFactory(builder);

		// finally create the HttpClient instance
		this.httpClient = builder.build();
		this.timeoutMs = timeoutMs;
		this.withAuth = false;
	}

	/**
	 * Return the current {@link HttpClient} instance.
	 *
	 * @return The internally used instance of the {@link HttpClient}
	 */
	public CloseableHttpClient getHttpClient() {
		return httpClient;
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
                str.set(IOUtils.toString(inputStream, "UTF-8"));
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
                str.set(IOUtils.toString(inputStream, "UTF-8"));
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

	private void simpleGetInternal(String url, Consumer<InputStream> consumer, String body) throws IOException {
        final HttpUriRequest httpGet;
        if(body == null) {
            httpGet = new HttpGet(url);
        } else {
            httpGet = new HttpGetWithBody(url);
            ((HttpGetWithBody)httpGet).setEntity(new StringEntity(body));
        }

        final CloseableHttpResponse execute;
        if(withAuth) {
            // Required to avoid two requests instead of one: See http://stackoverflow.com/questions/20914311/httpclientbuilder-basic-auth
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();

            // Generate BASIC scheme object and add it to the local auth cache
            URL cacheUrl = new URL(url);
            HttpHost targetHost = new HttpHost(cacheUrl.getHost(), cacheUrl.getPort(), cacheUrl.getProtocol());
            authCache.put(targetHost, basicAuth);

            // Add AuthCache to the execution context
            HttpClientContext context = HttpClientContext.create();
            //context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);

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
		// Required to avoid two requests instead of one: See http://stackoverflow.com/questions/20914311/httpclientbuilder-basic-auth
		final AuthCache authCache = new BasicAuthCache();
		final BasicScheme basicAuth = new BasicScheme();

		// Generate BASIC scheme object and add it to the local auth cache
		URL cacheUrl = new URL(url);
		HttpHost targetHost = new HttpHost(cacheUrl.getHost(), cacheUrl.getPort(), cacheUrl.getProtocol());
		authCache.put(targetHost, basicAuth);

		// Add AuthCache to the execution context
		HttpClientContext context = HttpClientContext.create();
		//context.setCredentialsProvider(credsProvider);
		context.setAuthCache(authCache);

		final HttpPost httpPost = new HttpPost(url);
		if(body != null) {
			httpPost.setEntity(new StringEntity(body));
		}
		try (CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context)) {
			HttpEntity entity = HttpClientWrapper.checkAndFetch(response, url);

			try {
				return IOUtils.toString(entity.getContent(), "UTF-8");
			} finally {
				// ensure all content is taken out to free resources
				EntityUtils.consume(entity);
			}
		}
	}

	private HttpClientBuilder createSSLSocketFactory(HttpClientBuilder builder) {
		try {
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
                    } }, new SecureRandom());

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
		return builder;
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
					StringUtils.abbreviate(IOUtils.toString(response.getEntity().getContent(), "UTF-8"), 1024);
            log.warning(msg);

            throw new IOException(msg);
        }

        return response.getEntity();
	}
}
