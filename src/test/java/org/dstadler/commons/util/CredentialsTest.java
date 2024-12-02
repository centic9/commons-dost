package org.dstadler.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.stream.IntStreams;
import org.junit.Test;

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
			assertNull(Credentials.getCredentialOrNull("user"));
			assertThrows(IllegalStateException.class,
					() -> Credentials.getCredentialOrFail("user"));

			Credentials.loadProperties(tempFile);

			assertNull(Credentials.getCredentialOrNull("user"));
			assertThrows(IllegalStateException.class,
					() -> Credentials.getCredentialOrFail("user"));

			FileUtils.writeStringToFile(tempFile, "user=abcd", "UTF-8");

			Credentials.loadProperties(tempFile);

			assertEquals("abcd", Credentials.getCredentialOrFail("user"));
			assertEquals("abcd", Credentials.getCredentialOrNull("user"));
			assertNull(Credentials.getCredentialOrNull("pwd"));

			Credentials.loadProperties(tempDir);

			assertEquals("abcd", Credentials.getCredentialOrFail("user"));
			assertEquals("abcd", Credentials.getCredentialOrNull("user"));

			FileUtils.writeStringToFile(tempFile, "user=efgh", "UTF-8");
			Credentials.loadProperties(tempFile);

			assertEquals("efgh", Credentials.getCredentialOrFail("user"));
			assertEquals("efgh", Credentials.getCredentialOrNull("user"));
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


	@Test
	public void testLoadInvalidFile() {
		Credentials.loadProperties(new File("invalid"));

		Credentials.loadProperties(new File("invalid!§\"$%$%=(/?)_:;,.-__;:Ä'Ö#äö+ü*Ü+`¸¸^°!"));

		Credentials.loadProperties(new File("."));
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(Credentials.class);
	}

	@Test
	public void testParallel() throws Throwable {
		AtomicReference<Throwable> exc = new AtomicReference<>();

		// load credentials in multiple streams to verify multi-threading
		IntStreams.range(100).asLongStream().
				parallel().
				forEach(value -> {
					// populate the file at some point
					if (value == 20) {
						try {
							File file = File.createTempFile("CredentialsTest", ".properties");
							try {
								FileUtils.writeStringToFile(file, """
										user=ab
										password=cdr
										""",
										StandardCharsets.UTF_8);
								Credentials.loadProperties(file);
							} finally {
								assertTrue("Could not delete file " + file,
										!file.exists() || file.delete());
							}
						} catch (Throwable e) {
							exc.set(e);
						}
					}
					Credentials.getCredentialOrNull("user");
					Credentials.getCredentialOrNull("password");
				});

		if (exc.get() != null) {
			throw exc.get();
		}
	}
}
