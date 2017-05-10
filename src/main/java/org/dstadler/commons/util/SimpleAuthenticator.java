package org.dstadler.commons.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class SimpleAuthenticator extends Authenticator {
	private static final ThreadLocal<String> username = new ThreadLocal<>(), password = new ThreadLocal<>();

	public SimpleAuthenticator(String username, String password) {
		SimpleAuthenticator.username.set(username);
		SimpleAuthenticator.password.set(password);
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username.get(), password.get().toCharArray());
	}
}
