package org.dstadler.commons.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;


public class TailInputStreamTest {

	@Test
	public void testTailInputStream() throws Exception {
		File file = File.createTempFile("TailInputStream", ".test");
		try {
			FileUtils.write(file, StringUtils.repeat("1234567890\n", 100));

			assertEquals(1100, file.length());

			// Expected is lower because the TailInputStream skips to the next newline to always show full lines
			assertCount(file, 11, 20);
			assertCount(file, 99, 100);
			assertCount(file, 990, 999);
			assertCount(file, 990, 1000);
			assertCount(file, 990, 1001);

			// now it returns full length
			assertCount(file, 1100, 2001);
		} finally {
			assertTrue(file.delete());
		}
	}

	@Test
	public void testTailInputStreamRN() throws Exception {
		File file = File.createTempFile("TailInputStream", ".test");
		try {
			FileUtils.write(file, StringUtils.repeat("1234567890\r\n", 100));

			assertEquals(1200, file.length());

			// Expected is lower because the TailInputStream skips to the next newline to always show full lines
			assertCount(file, 13, 20);
			assertCount(file, 97, 100);
			assertCount(file, 997, 999);
			assertCount(file, 997, 1000);
			assertCount(file, 997, 1001);

			// now it returns full length
			assertCount(file, 1200, 2001);
		} finally {
			assertTrue(file.delete());
		}
	}

	@Test
	public void testTailInputStreamR() throws Exception {
		File file = File.createTempFile("TailInputStream", ".test");
		try {
			FileUtils.write(file, StringUtils.repeat("1234567890\r", 100));

			assertEquals(1100, file.length());

			// Expected is lower because the TailInputStream skips to the next newline to always show full lines
			assertCount(file, 11, 20);
			assertCount(file, 99, 100);
			assertCount(file, 990, 999);
			assertCount(file, 990, 1000);
			assertCount(file, 990, 1001);

			// now it returns full length
			assertCount(file, 1100, 2001);
		} finally {
			assertTrue(file.delete());
		}
	}

	@Test
	public void testTailInputStreamRNOnly() throws Exception {
		File file = File.createTempFile("TailInputStream", ".test");
		try {
			FileUtils.write(file, StringUtils.repeat("\r\n", 100));

			assertEquals(200, file.length());

			// Expected is lower because the TailInputStream skips to the next newline to always show full lines
			assertCount(file, 19, 20);
			assertCount(file, 98, 99);
			assertCount(file, 99, 100);
			assertCount(file, 100, 101);

			// now it returns full length
			assertCount(file, 200, 2001);
		} finally {
			assertTrue(file.delete());
		}
	}

	@Test
	public void testTailInputStreamRead() throws Exception {
		File file = File.createTempFile("TailInputStream", ".test");
		try {
			FileUtils.write(file, StringUtils.repeat("1234567890\r", 100));

			assertEquals(1100, file.length());

			try (InputStream stream = new TailInputStream(file, 20)) {
				assertEquals(11, stream.read(new byte[1000], 0, 1000));
			}

			try (InputStream stream = new TailInputStream(file, 20)) {
				assertEquals(10, stream.skip(10));
				assertEquals(1, stream.read(new byte[1000], 0, 1000));

				assertEquals(-1, stream.read());
			}
		} finally {
			assertTrue(file.delete());
		}
	}
	protected void assertCount(File file, int expected, int read) throws IOException {
		try (InputStream stream = new TailInputStream(file, read)) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				IOUtils.copy(stream, out);

				assertEquals("Had: " + new String(out.toByteArray()),
						expected, out.toByteArray().length);
			}
		}
	}
}
