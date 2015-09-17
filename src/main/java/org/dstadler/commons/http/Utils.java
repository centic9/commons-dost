package org.dstadler.commons.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {
	private static final Logger logger = Logger.getLogger(Utils.class.toString());

	private static Random rand = new Random();

	public static void setSeed(final long seed) {
		rand.setSeed(seed);
	}

	public static String getRandomURL() {
		// use binary host to have a better chance of finding something

		// randomstring('a', 'z')

		StringBuilder str = new StringBuilder("http://");

		int i1;
		// loop until we have a valid first item
		while (true)
		{
			i1 = rand.nextInt(256);
			// see IP-Adress in Wikipedia at http://de.wikipedia.org/wiki/IP-Adresse
			if(i1 != 0 && i1 != 10 && i1 != 14 && i1 != 39
					&& i1 != 127 &&
					i1 < 224	// alles oberhalb ist reserviert!
			) {
				break;
			}
		}
		str.append(i1);
		str.append('.');

		int i2;
		// then loop on the second item until we have a valid first and second item
		while (true)
		{
			i2 = rand.nextInt(256);
			// see IP-Adress in Wikipedia at http://de.wikipedia.org/wiki/IP-Adresse
			if(
					(i1 != 128 || i2 != 0) &&
					(i1 != 169 || i2 != 254) &&
					(i1 != 172 || i2 < 16 || i2 > 31) &&
					(i1 != 191 || i2 != 255) &&
					(i1 != 192 || i2 != 168) &&
					(i1 != 198 || i2 != 18) &&
					(i1 != 198 || i2 != 19)
				) {
				break;
			}
		}
		str.append(i2);
		str.append('.');

		/*
CIDR-Adressblock 	Adressbereich 	Beschreibung 	RFC
192.0.0.0/24 	192.0.0.0 bis 192.0.0.255 	durch IANA reserviert
192.0.2.0/24 	192.0.2.0 bis 192.0.2.255 	Dokumentation und Beispielcode (TEST-NET) 	RFC 3330
192.88.99.0/24 	192.88.99.0 bis 192.88.99.255 	6to4-Anycast-Weiterleitungspr�fix 	RFC 3068
223.255.255.0/24 	223.255.255.0 bis 223.255.255.255 	Reserviert 	RFC 3330
255.255.255.255) 	255.255.255.255 	Broadcast
*/

		str.append(rand.nextInt(256));
		str.append('.');
		str.append(rand.nextInt(256));
		str.append('/');

		return str.toString();
		//return "http://213.165.65.50/";
	}

	public static IP getRandomIP() {
		int i1;
		// loop until we have a valid first item
		while (true)
		{
			i1 = rand.nextInt(256);
			// see IP-Adress in Wikipedia at http://de.wikipedia.org/wiki/IP-Adresse
			if(i1 != 0 && i1 != 10 && i1 != 14 && i1 != 39
					&& i1 != 127 &&
					i1 < 224	// alles oberhalb ist reserviert!
			) {
				break;
			}
		}

		int i2;
		// then loop on the second item until we have a valid first and second item
		while (true)
		{
			i2 = rand.nextInt(256);
			// see IP-Adress in Wikipedia at http://de.wikipedia.org/wiki/IP-Adresse
			if(
					(i1 != 128 || i2 != 0) &&
					(i1 != 169 || i2 != 254) &&
					(i1 != 172 || i2 < 16 || i2 > 31) &&
					(i1 != 191 || i2 != 255) &&
					(i1 != 192 || i2 != 168) &&
					(i1 != 198 || i2 != 18) &&
					(i1 != 198 || i2 != 19)
				) {
				break;
			}
		}

		/*
CIDR-Adressblock 	Adressbereich 	Beschreibung 	RFC
192.0.0.0/24 	192.0.0.0 bis 192.0.0.255 	durch IANA reserviert
192.0.2.0/24 	192.0.2.0 bis 192.0.2.255 	Dokumentation und Beispielcode (TEST-NET) 	RFC 3330
192.88.99.0/24 	192.88.99.0 bis 192.88.99.255 	6to4-Anycast-Weiterleitungspr�fix 	RFC 3068
223.255.255.0/24 	223.255.255.0 bis 223.255.255.255 	Reserviert 	RFC 3330
255.255.255.255) 	255.255.255.255 	Broadcast
*/

		return new IP(i1, i2, rand.nextInt(256), rand.nextInt(256));
	}

	public static boolean isIgnorableException(Exception e) {
		if(e instanceof NoRouteToHostException) {
			return true;
		}

		if(e instanceof SocketTimeoutException) {
			return true;
		}

		if(e.toString().contains("Connection timed out")) {
			return true;
		}

		if(e.toString().contains("Network is unreachable")) {	// TODO: why do we get this so often?
			return true;
		}

		if(e.toString().contains("Connection refused")) {
			return true;
		}

		if(e.toString().contains("Server returned HTTP response code: 403")) {
			return true;
		}

		if(e.toString().contains("Server returned HTTP response code: 401")) {
			return true;
		}

//		if(e.toString().contains("Network is unreachable"))	{
//			return true;
//		}

//		if(e.toString().contains("No route to host")) {
//			return true;
//		}

//		if(e.toString().contains("connect timed out")) {
//			return true;
//		}

		return false;
	}

	/**
	 * Test URL and report if it can be read.
	 *
	 * @return true if the URL is valid and can be read, false if an error occurs when reading from
	 * it.
	 */
	public static boolean getURL(final String sUrl, final AtomicInteger gCount, long start) {
		int count = gCount.incrementAndGet();
		if(count % 100 == 0) {
			long diff = (System.currentTimeMillis() - start)/1000;
			logger.info("Count: " + count + " IPS: " + count/diff);
		}

		final URL url;
		try {
			url = new URL(sUrl);
		} catch (MalformedURLException e) {
			logger.info("URL-Failed(" + count + "): " + e.toString());
			return false;
		}

		logger.log(Level.FINE, "Testing(" + count + "): " + url);
		final URLConnection con;
		try {
			con = url.openConnection();
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);
			con.getInputStream().read();
		} catch (IOException e) {
			// don't print out time out as it is expected here
			if(Utils.isIgnorableException(e)) {
				return false;
			}

			logger.log(Level.WARNING, "Failed (" + url + ")(" + count + ")", e);
			return false;
		}

		//logger.info(con);
//		logger.info("Date            : " + new Date(con.getDate()));
		logger.info("Last Modified (" + url + ")(" + count + "): " + new Date(con.getLastModified()));
		// logger.info( "Content encoding: " + con.getContentEncoding()
		// );
		// logger.info( "Content type : " + con.getContentType() );
		// logger.info( "Content length : " + con.getContentLength() );

		return true;
	}
}
