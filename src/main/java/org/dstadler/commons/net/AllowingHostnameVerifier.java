package org.dstadler.commons.net;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Allows to connect to HTTPS urls without verifying the host mentioned in the SSL certificate.
 */
public final class AllowingHostnameVerifier implements HostnameVerifier {
	private static final HostnameVerifier INSTANCE = new AllowingHostnameVerifier();

	public static HostnameVerifier instance() {
		return INSTANCE;
	}

	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}
}
