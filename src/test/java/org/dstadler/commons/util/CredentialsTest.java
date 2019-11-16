package org.dstadler.commons.util;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CredentialsTest {

	@Test
	public void testLoadCredentials() {
		assertNotNull(Credentials.loadCredentials());
	}

	@Test
	public void testLoadProperties() throws IOException {
		// to initialize
		Credentials.loadCredentials();

		File tempFile = File.createTempFile("CredentialsTest", ".properties");
		File tempDir = File.createTempFile("CredentialsTest", ".properties");
		assertTrue(tempDir.delete());
		assertTrue(tempDir.mkdirs());
		try {
			Credentials.loadProperties(tempFile);

			FileUtils.writeStringToFile(tempFile, "user=abcd", "UTF-8");

			Credentials.loadProperties(tempFile);

			assertEquals("abcd", Credentials.getCredentialOrFail("user"));
			assertEquals("abcd", Credentials.getCredentialOrNull("user"));
			assertNull(Credentials.getCredentialOrNull("pwd"));

			Credentials.loadProperties(tempDir);
		} finally {
			FileUtils.deleteQuietly(tempFile);
			FileUtils.deleteQuietly(tempDir);
		}
	}

	@Test
	public void testGetCredentialOrNull() {
		// we don't store any in Git, so we can only test the missing ones
		assertNull(Credentials.getCredentialOrNull("somenonexistingkey"));
	}

	@Test
	public void testGetCredentialOrFail() {
		// we don't store any in Git, so we can only test the missing ones
		try {
			Credentials.getCredentialOrFail("somenonexistingkey");
			fail("Should fail here");
		} catch (@SuppressWarnings("unused") IllegalStateException e) {
			// expected here
		}
	}
}
