package org.dstadler.commons.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.dstadler.commons.http5.HttpClientWrapper5;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple SAX Parser to handle XML data, it is usually subclassed to provide the
 * missing pieces of information to actually collect some information out of the XML data.
 */
public abstract class AbstractSimpleContentHandler<K extends Comparable<K>,V> extends DefaultHandler {
	private final static Logger log = LoggerFactory.make();

	// use TreeMap to sort by key
	protected SortedMap<K, V> configs = new TreeMap<>();

	protected V currentTags = null;

	protected StringBuilder characters = new StringBuilder(100);

	public SortedMap<K, V> parseContent(URL url, String user, String password, int timeoutMs) throws IOException, SAXException {
		log.info("Using the following URL for retrieving the list of elements: " + url.toString());

		try (final HttpClientWrapper5 httpClient = new HttpClientWrapper5(user, password, timeoutMs)) {
			final HttpGet httpGet = new HttpGet(url.toURI());
            return httpClient.getHttpClient().execute(httpGet, (HttpClientResponseHandler<SortedMap<K, V>>) response -> {
                int statusCode = response.getCode();
                if(statusCode != 200) {
                    String msg = "Had HTTP StatusCode " + statusCode + " for request: " + url + ", response: " + response.getReasonPhrase();
                    log.warning(msg);

                    throw new IOException(msg);
                }
                HttpEntity entity = response.getEntity();

                try {
                    return parseContent(entity.getContent());
                } catch (SAXException e) {
                    throw new IOException(e);
                } finally {
                    // ensure all content is taken out to free resources
                    EntityUtils.consume(entity);
                }
            });
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} catch (IOException e) {
            if (e.getCause() instanceof SAXException) {
                throw (SAXException) e.getCause();
            }

            throw e;
        }
	}

	public SortedMap<K, V> parseContent(InputStream strm) throws SAXException, IOException {
		// clean up before starting parse to avoid multiple parse steps with the same object
		configs.clear();
		currentTags = null;
		characters.setLength(0);

		final XMLReader parser = XMLHelper.newXMLReader();

		parser.setContentHandler(this);
		parser.setErrorHandler(this);

		InputSource source = new InputSource(strm);

		adjustParser(parser);

		parser.parse(source);

		return getConfigs();
	}

	/**
	 * Callback method which allows to adjust the parser, e.g.
	 * by adjusting additional features.
	 *
	 * @param parser The {@link XMLReader} used for parsing the XML.
	 *
	 * @throws org.xml.sax.SAXException When parsing XML fails
	 * @throws java.io.IOException When an I/O error occurs
	 */
	@SuppressWarnings({"RedundantThrows", "unused"})
	protected void adjustParser(XMLReader parser) throws SAXException, IOException {
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		// combine characters for later use
		characters.append(ch, start, length);
	}

	@Override
	public void error(SAXParseException exception) {
		log.log(Level.SEVERE, "Error in SAX Parsing", exception);
	}

	@Override
	public void fatalError(SAXParseException exception) {
		log.log(Level.SEVERE, "Error in SAX Parsing", exception);
	}

	@Override
	public void warning(SAXParseException exception) {
		log.log(Level.WARNING, "Error in SAX Parsing", exception);
	}

	/**
	 * @return the issues
	 */
	public SortedMap<K, V> getConfigs() {
		return configs;
	}
}
