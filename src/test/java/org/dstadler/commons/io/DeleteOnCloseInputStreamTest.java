package org.dstadler.commons.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.Test;

public class DeleteOnCloseInputStreamTest {

	@Test
	public void testDeleteOnCloseInputStream() throws Exception {
		File file = File.createTempFile("deleteOnClose1_", ".test");
		FileUtils.writeStringToFile(file,
				"somedata to have something to read...");
		try (InputStream input = new FileInputStream(file)) {
			assertTrue(file.exists());
			assertTrue(file.length() > 0);

			assertTrue(input.available() > 0);

			try (DeleteOnCloseInputStream stream = new DeleteOnCloseInputStream(input,
					file)) {
				assertTrue(stream.available() > 0);

				assertTrue(file.exists());
				assertTrue(file.length() > 0);

				List<String> lines = IOUtils.readLines(stream);
				assertEquals(1, lines.size());

				assertTrue(file.exists());
				assertTrue(file.length() > 0);
			}

			assertFalse("After closing, the file should be deleted from disk.",
					file.exists());
		}
	}

	@Test
	public void testCoverMethods() throws Exception {
		File file = File.createTempFile("deleteOnClose2_", ".test");
		FileUtils.writeStringToFile(file,
				"somedata to have something to read...");
		try (InputStream input = new FileInputStream(file)) {
			try (DeleteOnCloseInputStream stream = new DeleteOnCloseInputStream(input,
					file)) {
				assertTrue(stream.available() > 0);

				assertTrue(file.exists());
				assertTrue(file.length() > 0);

				try {
					stream.reset();
					fail("Mark/Reset not supported");
				} catch (IOException e) {
					TestHelpers.assertContains(e, "mark/reset not supported");
				}

				// cover the overwritten methods
				assertEquals('s', stream.read());
				assertEquals(2, stream.read(new byte[2]));

				stream.mark(2);

				stream.skip(1);
				try {
					stream.reset();
					fail("Mark/Reset not supported");
				} catch (IOException e) {
					TestHelpers.assertContains(e, "mark/reset not supported");
				}
				assertFalse(stream.markSupported());
			}

			assertFalse("After closing, the file should be deleted from disk.",
					file.exists());
		}
	}

	@Test
	public void testReset() throws Exception {
		File file = File.createTempFile("deleteOnClose3_", ".test");
		FileUtils.writeStringToFile(file,
				"somedata to have something to read...");
		try (InputStream input = new FileInputStream(file) {
			@Override
			public synchronized void reset() throws IOException {
				// just do nothing to make reset() supported here
			}
		}) {
			try (DeleteOnCloseInputStream stream = new DeleteOnCloseInputStream(input,
					file)) {
				stream.reset();
			}

			assertFalse("After closing, the file should be deleted from disk.",
					file.exists());
		}
	}

	@Test
	public void testCloseFileError() throws IOException {
		File dir = File.createTempFile("deleteOnClose4_", ".test");
		assertTrue(dir.delete());
		assertTrue(dir.mkdirs());

		File file = File.createTempFile("deleteOnClose5_", ".test", dir);

		try (InputStream input = new FileInputStream(file)) {
			try (DeleteOnCloseInputStream stream = new DeleteOnCloseInputStream(input, dir)) {
				stream.close();
			}

			// try to clean up, file was not removed as we passed the dir to the stream!
			assertTrue(file.delete());
			assertTrue(dir.delete());
		}
	}

	@Test
	public void testNullDelegate() throws IOException {
		try {
			// fail-fast with an NPE in the constructor already, not later when we do not see any more where it was coming from
			try (InputStream stream = new DeleteOnCloseInputStream(null, new File("."))) {
				assertNotNull(stream);
			}
			fail("Should catch exception here");
		} catch (@SuppressWarnings("unused") NullPointerException e) {
			// expected here
		}
	}
}
