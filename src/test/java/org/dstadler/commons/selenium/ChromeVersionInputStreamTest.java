package org.dstadler.commons.selenium;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class ChromeVersionInputStreamTest {
	@Test
	void testMatchingUrlExtraction() throws Exception {
		String input = """
				Some text before
				https://storage.googleapis.com/chrome-for-testing-public/114.0.5735.199123/linux64/chromedriver-linux64.zip
				Some text after
				""";

		ChromeVersionInputStream in = new ChromeVersionInputStream(
				new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
				"114");
		in.consumeAndClose();

		assertEquals("114.0.5735.199123", in.getDriverVersion());
		assertEquals("storage.googleapis.com/chrome-for-testing-public", in.getUrl());
	}

	@Test
	void testMatchingUrlExtractionMultipleMatches() throws Exception {
		// use multiple matching URLs to verify that the last one is taken
		String input = """
				Some text before
				https://storage.googleapis.com/chrome-for-testing-public/114.1.5735.199123/linux64/chromedriver-linux64.zip
				https://storage.googleapis.com/chrome-for-testing-public/114.2.5735.199123/linux64/chromedriver-linux64.zip
				https://storage.googleapis.com/chrome-for-testing-public/114.3.5735.199123/linux64/chromedriver-linux64.zip
				https://storage.googleapis.com/chrome-for-testing-public/114.4.5735.199123/linux64/chromedriver-linux64.zip
				Some text after
				""";

		ChromeVersionInputStream in = new ChromeVersionInputStream(
				new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
				"114");
		in.consumeAndClose();

		assertEquals("114.4.5735.199123", in.getDriverVersion());
		assertEquals("storage.googleapis.com/chrome-for-testing-public", in.getUrl());
	}

	@Test
	void testMatchingUrlExtractionWithSingleReads() throws Exception {
		String input = """
				Some text before
				https://storage.googleapis.com/chrome-for-testing-public/114.0.5735.199123/linux64/chromedriver-linux64.zip
				Some text after
				""";

		ChromeVersionInputStream in = new ChromeVersionInputStream(
				new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
				"114");

		// consume and close the buffer manually in this test to verify the method "int read()" as well
		while(true) {
			// just read until eof
			if (in.read() == -1) {
				break;
			}
		}
		in.close();

		assertEquals("114.0.5735.199123", in.getDriverVersion());
		assertEquals("storage.googleapis.com/chrome-for-testing-public", in.getUrl());
	}

	@Test
	void testNonMatchingUrlExtraction() throws Exception {
		String input = """
				Some text before
				Some text after
				""";

		ChromeVersionInputStream in = new ChromeVersionInputStream(
				new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
				"114");
		IOUtils.consume(in);
		in.close();

		assertNull(in.getDriverVersion());
		assertNull(in.getUrl());
	}
}
