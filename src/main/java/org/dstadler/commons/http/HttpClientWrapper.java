package org.dstadler.commons.http;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
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
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
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
 * on webpages.
 *
 * @author dominik.stadler
 */
public class HttpClientWrapper implements Closeable {
	private final static Logger log = LoggerFactory.make();

	private final CredentialsProvider credsProvider = new BasicCredentialsProvider();
	private final CloseableHttpClient httpClient;

	private final int timeoutMs;

	/**
	 * Construct the {@link HttpClient} with the given authentication values
	 * and all timeouts set to the given number of milliseconds
	 *
	 * @param user
	 * @param password
	 * @param timeoutMs
	 */
	public HttpClientWrapper(String user, String password, int timeoutMs) {
		super();

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
	 * Throws an IOException if the HTTP status code is not 200.
	 *
	 * @param url
	 * @return The data returned when retrieving the data from the given url, converted to a String.
	 * @throws IOException
	 */
	public String simpleGet(String url) throws IOException {
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

		final HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(targetHost, httpGet, context)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode != 200) {
				String msg = "Had HTTP StatusCode " + statusCode + " for request: " + url + ", response: " + response.getStatusLine().getReasonPhrase();
				log.warning(msg);

				throw new IOException(msg);
			}
		    HttpEntity entity = response.getEntity();

		    try {
		    	return IOUtils.toString(entity.getContent());
		    } finally {
			    // ensure all content is taken out to free resources
			    EntityUtils.consume(entity);
		    }
		}
	}

	public byte[] simpleGetBytes(String url) throws IOException {
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

		final HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(targetHost, httpGet, context)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode != 200) {
				String msg = "Had HTTP StatusCode " + statusCode + " for request: " + url + ", response: " + response.getStatusLine().getReasonPhrase();
				log.warning(msg);

				throw new IOException(msg);
			}
		    HttpEntity entity = response.getEntity();

		    try {
		    	return IOUtils.toByteArray(entity.getContent());
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
	                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER) {

						@Override
						protected void prepareSocket(SSLSocket socket) throws IOException {
							super.prepareSocket(socket);

							// workaround for https://issues.apache.org/jira/browse/HTTPCLIENT-1478
							socket.setSoTimeout(timeoutMs);

							// when running with Java 6 we should remove the outdated SSLv2Hello protocol as it is not
							// supported any more by dynaTrace after applying the FixPack for the POODLE SSLv3 attack
							Set<String> protocols = new HashSet<>(Arrays.asList(socket.getEnabledProtocols()));
							protocols.remove("SSLv2Hello");
							socket.setEnabledProtocols(protocols.toArray(new String[protocols.size()]));
						}

	        };

			builder = builder.setSSLSocketFactory(sslsf);
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
	 * @throws IOException if the HTTP status code is higher than 206.
	 */
    public static HttpEntity checkAndFetch(HttpResponse response, String url) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode > 206) {
            String msg = "Had HTTP StatusCode " + statusCode + " for request: " + url + ", response: " + 
                    response.getStatusLine().getReasonPhrase();
            log.warning(msg);
   
            throw new IOException(msg);
        }
        return response.getEntity();
    }
}
