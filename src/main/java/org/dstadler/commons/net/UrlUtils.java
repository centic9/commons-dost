package org.dstadler.commons.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.IOUtils;
import org.dstadler.commons.logging.jdk.LoggerFactory;


/**
 * Collection of simple utility-methods for accessing network resources.
 *
 * For more sophisticated access use {@link org.dstadler.commons.http.HttpClientWrapper}.
 */
public class UrlUtils {
    private static final Logger LOGGER = LoggerFactory.make();

    /*
     * The first n bytes of data to print to FINE in retrieveRawData()
     */
    private static final int REPORT_PEEK_COUNT = 200;

    /**
     * Download data from an URL.
     *
     * @param sUrl The full URL used to download the content.
     * @param timeout The timeout in milliseconds that is used for both
     *         connection timeout and read timeout.
     *
     * @return The resulting data, e.g. a HTML string.
     *
     * @throws IOException If accessing the resource fails.
     */
    public static String retrieveData(String sUrl, int timeout) throws IOException {
        return retrieveData(sUrl, null, timeout);
    }

    /**
     * Download data from an URL, if necessary converting from a character encoding.
     *
     * @param sUrl The full URL used to download the content.
     * @param encoding An encoding, e.g. UTF-8, ISO-8859-15. Can be null.
     * @param timeout The timeout in milliseconds that is used for both
     *         connection timeout and read timeout.
     *
     * @return The resulting data, e.g. a HTML string.
     *
     * @throws IOException If accessing the resource fails.
     */
    public static String retrieveData(String sUrl, String encoding, int timeout) throws IOException {
        return retrieveData(sUrl, encoding, timeout, null);
    }

    /**
     * Download data from an URL, if necessary converting from a character encoding.
     *
     * @param sUrl The full URL used to download the content.
     * @param encoding An encoding, e.g. UTF-8, ISO-8859-15. Can be null.
     * @param timeout The timeout in milliseconds that is used for both
     *         connection timeout and read timeout.
     * @param sslFactory The SSLFactory to use for the connection, this allows to support custom SSL certificates
     *
     * @return The resulting data, e.g. a HTML string.
     *
     * @throws IOException If accessing the resource fails.
     */
    public static String retrieveData(String sUrl, String encoding, int timeout, SSLSocketFactory sslFactory) throws IOException {
        byte[] rawData = retrieveRawData(sUrl, timeout, sslFactory);
        if(encoding == null) {
            return new String(rawData);  // NOSONAR
        }

        return new String(rawData, encoding);
    }

    /**
     * Download data from an URL and return the raw bytes.
     *
     * @param sUrl The full URL used to download the content.
     * @param timeout The timeout in milliseconds that is used for both
     *         connection timeout and read timeout.
     *
     * @return The resulting data, e.g. a HTML string as byte array.
     *
     * @throws IOException If accessing the resource fails.
     */
    public static byte[] retrieveRawData(String sUrl, int timeout) throws IOException {
        return retrieveRawData(sUrl, timeout, null);
    }

    /**
     * Download data from an URL and return the raw bytes.
     *
     * @param sUrl The full URL used to download the content.
     * @param timeout The timeout in milliseconds that is used for both
     *         connection timeout and read timeout.
     * @param sslFactory The SSLFactory to use for the connection, this allows to support custom SSL certificates
     *
     * @return The resulting data, e.g. a HTML string as byte array.
     *
     * @throws IOException If accessing the resource fails.
     */
    public static byte[] retrieveRawData(String sUrl, int timeout, SSLSocketFactory sslFactory) throws IOException {
        URL url = new URL(sUrl);

        LOGGER.fine("Using the following URL for retrieving the data: " + url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // set specified timeout if non-zero
        if(timeout != 0) {
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
        }

        try {
            conn.setDoOutput(false);
            conn.setDoInput(true);

            if(conn instanceof HttpsURLConnection && sslFactory != null) {
                ((HttpsURLConnection)conn).setSSLSocketFactory(sslFactory);
            }

            conn.connect();
            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK &&
                    code != HttpURLConnection.HTTP_CREATED &&
                    code != HttpURLConnection.HTTP_ACCEPTED) {

                String msg = "Error " + code + " returned while retrieving response for url '" + url
                        + "' message from client: " + conn.getResponseMessage();
                LOGGER.warning(msg);

                throw new IOException(msg);
            }

            try (InputStream strm = conn.getInputStream()) {
                return IOUtils.toByteArray(strm);
            }
            // actually read the contents, even if we are not using it to simulate a full download of the data
            /*ByteArrayOutputStream memStream = new ByteArrayOutputStream(conn.getContentLength() == -1 ? 40000 : conn.getContentLength());
            try {
                byte b[] = new byte[4096];
                int len;
                while ((len = strm.read(b)) > 0) {
                    memStream.write(b, 0, len);
                }
            } finally {
                memStream.close();
            }

            if(LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Received data, size: " + memStream.size() + "(" + conn.getContentLength() + ") first bytes: "
                        + replaceInvalidChar(memStream.toString().substring(0, Math.min(memStream.size(), REPORT_PEEK_COUNT))));
            }

            return memStream.toByteArray();*/
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Download data from an URL with a POST request, if necessary converting from a character encoding.
     *
     * @param sUrl The full URL used to download the content.
     * @param encoding An encoding, e.g. UTF-8, ISO-8859-15. Can be null.
     * @param postRequestBody the body of the POST request, e.g. request parameters; must not be null
     * @param contentType the content-type of the POST request; may be null
     * @param timeout The timeout in milliseconds that is used for both connection timeout and read timeout.
     * @return The response from the HTTP POST call.
     * @throws IOException If accessing the resource fails.
     */
    public static String retrieveDataPost(String sUrl, String encoding, String postRequestBody, String contentType, int timeout) throws IOException {
        return retrieveStringInternalPost(sUrl, encoding, postRequestBody, contentType, timeout, null);
    }

    /**
     * Download data from an URL with a POST request, if necessary converting from a character encoding.
     *
     * @param sUrl The full URL used to download the content.
     * @param encoding An encoding, e.g. UTF-8, ISO-8859-15. Can be null.
     * @param postRequestBody the body of the POST request, e.g. request parameters; must not be null
     * @param contentType the content-type of the POST request; may be null
     * @param timeout The timeout in milliseconds that is used for both connection timeout and read timeout.
     * @param sslFactory The SSLFactory to use for the connection, this allows to support custom SSL certificates
     * @return The response from the HTTP POST call.
     * @throws IOException If accessing the resource fails.
     */
    public static String retrieveDataPost(String sUrl, String encoding, String postRequestBody, String contentType, int timeout, SSLSocketFactory sslFactory) throws IOException {
        return retrieveStringInternalPost(sUrl, encoding, postRequestBody, contentType, timeout, sslFactory);
    }

    private static String retrieveStringInternalPost(String sUrl, String encoding, String postRequestBody, String contentType, int timeout, SSLSocketFactory sslFactory) throws IOException {
        byte[] rawData = retrieveRawInternalPost(sUrl, postRequestBody, contentType, timeout, sslFactory);
        return encoding != null ? new String(rawData, encoding) : new String(rawData);  // NOSONAR
    }

    private static byte[] retrieveRawInternalPost(String sUrl, String postRequestBody, String contentType, int timeout, SSLSocketFactory sslFactory) throws IOException {
        if (postRequestBody == null) {
            throw new IllegalArgumentException("POST request body must not be null");
        }

        URL url = new URL(sUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            prepareConnection(connection, timeout, sslFactory);

            writePostRequest(connection, postRequestBody, contentType);

            connection.connect();
            int responseCode = connection.getResponseCode();
            if (!responseCodeValid(responseCode)) {
                String message = "Error " + responseCode + " returned while retrieving response for url " + url + ", response message: " + connection.getResponseMessage();
                LOGGER.warning(message);
                throw new IOException(message);
            }

            // actually read the contents, even if we are not using it to simulate a full download of the data
            try (ByteArrayOutputStream memStream = new ByteArrayOutputStream(connection.getContentLength() == -1 ?
                    40000 : connection.getContentLength())) {
                try (InputStream in = connection.getInputStream()) {
                    IOUtils.copy(in, memStream);
                }

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Received data, size: " + memStream.size() +
                            " (" + connection.getContentLength() + ") first bytes: " +
                            replaceInvalidChars(new String(memStream.toByteArray(), 0,
                                    Math.min(memStream.size(), REPORT_PEEK_COUNT), StandardCharsets.US_ASCII)));
                }

                return memStream.toByteArray();
            }
        } finally {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Retrieved URL: " + url + ", header fields: " + connection.getHeaderFields());
            }
            connection.disconnect();
        }
    }

    /*
     * helper for logging binary content
     */
    private static String replaceInvalidChars(String substring) {
        StringBuilder builder = new StringBuilder();
        for(char c : substring.toCharArray()) {
            if(c < 32) {
                builder.append('.');
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /*
     * helper to decide if response is is considered valid for retrieveData.
     */
    private static boolean responseCodeValid(int responseCode) {
        return responseCode / 100 == HttpURLConnection.HTTP_OK / 100;
    }

    /*
     * Prepare connection by setting connectTimeout and readTimeout to timeout,
     * doOutput to false and doInput to true.
     *
     * Throws IllegalArgumentException on zero (infinite) timeout.
     */
    private static void prepareConnection(URLConnection connection, int timeout, SSLSocketFactory sslFactory) {
        if (timeout == 0) {
            throw new IllegalArgumentException("Zero (infinite) timeouts not permitted");
        }
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setDoInput(true); // whether we want to read from the connection
        connection.setDoOutput(false); // whether we want to write to the connection

        if(connection instanceof HttpsURLConnection && sslFactory != null) {
            ((HttpsURLConnection)connection).setSSLSocketFactory(sslFactory);
        }
    }

    /*
     * Write POST request header and body
     */
    private static void writePostRequest(URLConnection connection, String postRequestBody, String contentType) throws IOException {
        connection.setDoOutput(true); // whether we want to write to the connection

        if (contentType != null) {
            connection.setRequestProperty("Content-Type", contentType);
        }
        // Note: Content-Length is set implicitly by URLConnection
        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            out.writeBytes(postRequestBody);
        }
    }

    /**
     * Check if the HTTP resource specified by the destination URL is available.
     *
     * @param destinationUrl the destination URL to check for availability
     * @param fireRequest if true a request will be sent to the given URL in addition to opening the
     *        connection
     * @param timeout Timeout in milliseconds after which the call fails because of timeout.
     * @return <code>true</code> if a connection could be set up and the response was received
     * @throws IllegalArgumentException if the destination URL is invalid
     */
    public static boolean isAvailable(String destinationUrl, boolean fireRequest, int timeout) throws IllegalArgumentException {
        return isAvailable(destinationUrl, fireRequest, false, timeout);
    }

    /**
     * Check if the HTTP resource specified by the destination URL is available.
     *
     * @param destinationUrl the destination URL to check for availability
     * @param fireRequest if true a request will be sent to the given URL in addition to opening the
     *        connection
     * @param ignoreHTTPSHostCheck if specified true, a HostnameVerifier is registered which accepts all host-names
     *                             during SSL handshake
     * @param timeout Timeout in milliseconds after which the call fails because of timeout.
     * @return <code>true</code> if a connection could be set up and the response was received
     * @throws IllegalArgumentException if the destination URL is invalid
     */
    public static boolean isAvailable(String destinationUrl, boolean fireRequest, boolean ignoreHTTPSHostCheck,
                                      int timeout) throws IllegalArgumentException {
        return getAccessError(destinationUrl, fireRequest, ignoreHTTPSHostCheck, timeout, null) == null;
    }

    /**
     * Check if the HTTP resource specified by the destination URL is available.
     *
     * @param destinationUrl the destination URL to check for availability
     * @param fireRequest if true a request will be sent to the given URL in addition to opening the
     *        connection
     * @param ignoreHTTPSHostCheck if specified true, a HostnameVerifier is registered which accepts all host-names
     *                             during SSL handshake
     * @param timeout Timeout in milliseconds after which the call fails because of timeout.
     * @param sslFactory The SSLFactory to use for the connection, this allows to support custom SSL certificates
     * @return <code>true</code> if a connection could be set up and the response was received
     * @throws IllegalArgumentException if the destination URL is invalid
     */
    public static boolean isAvailable(String destinationUrl, boolean fireRequest, boolean ignoreHTTPSHostCheck,
                                      int timeout, SSLSocketFactory sslFactory) throws IllegalArgumentException {
        return getAccessError(destinationUrl, fireRequest, ignoreHTTPSHostCheck, timeout, sslFactory) == null;
    }

    /**
    *
    * @param destinationUrl the destination URL to check for availability
    * @param fireRequest if true a request will be sent to the given URL in addition to opening the
    *        connection
    * @param ignoreHTTPSHostCheck if specified true, a HostnameVerifier is registered which accepts all host-names
     *                             during SSL handshake
    * @param timeout Timeout in milliseconds after which the call fails because of timeout.
    * @return null if connection works, an error message if some problem happens.
     * @throws IllegalArgumentException if the destination URL is invalid
    */
   public static String getAccessError(String destinationUrl, boolean fireRequest, boolean ignoreHTTPSHostCheck, int timeout) throws IllegalArgumentException {
       return getAccessError(destinationUrl, fireRequest, ignoreHTTPSHostCheck, timeout, null);
   }

    /**
     *
     * @param destinationUrl the destination URL to check for availability
     * @param fireRequest if true a request will be sent to the given URL in addition to opening the
     *        connection
     * @param ignoreHTTPSHostCheck if specified true, a HostnameVerifier is registered which accepts all host-names
     *                             during SSL handshake
     * @param timeout Timeout in milliseconds after which the call fails because of timeout.
     * @param sslFactory The SSLFactory to use for the connection, this allows to support custom SSL certificates
     * @return null if connection works, an error message if some problem happens.
     * @throws IllegalArgumentException if the destination URL is invalid
     */
    public static String getAccessError(String destinationUrl, boolean fireRequest, boolean ignoreHTTPSHostCheck, int timeout, SSLSocketFactory sslFactory) throws IllegalArgumentException {
        URL url;
        try {
            url = new URL(destinationUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid destination URL", e);
        }

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();

            // set specified timeout if non-zero
            if(timeout != 0) {
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
            }

            if(ignoreHTTPSHostCheck && conn instanceof HttpsURLConnection) {
                ((HttpsURLConnection)conn).setHostnameVerifier(new AllowingHostnameVerifier());
            }
            if(conn instanceof HttpsURLConnection && sslFactory != null) {
                ((HttpsURLConnection)conn).setSSLSocketFactory(sslFactory);
            }

            conn.setDoOutput(false);
            conn.setDoInput(true);

            /* if connecting is not possible this will throw a connection refused exception */
            conn.connect();

            /* dotNet processes require a first request to be sent to initialize the application */
            if (fireRequest) {
                conn.getInputStream().close();
            }

            /* if connecting is possible we return true here */
            return null;
        } catch (IOException e) {
            if(LOGGER.isLoggable(Level.FINE)){
                LOGGER.fine("Connection attempt to '" + destinationUrl + "' failed. Connection refused.");
            }

            /* exception is thrown -> server not available */
            return e.getClass().getName() + ": " + e.getMessage();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
