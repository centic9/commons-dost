package org.dstadler.commons.zip;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import org.dstadler.commons.testing.TestHelpers;



/**
 *
 * @author dominik.stadler
 */
public class ZipFileCloseInputStreamTest {

	@Test
	public void testZipFileCloseInputStream() throws Exception {
		String name = null;
		try (ZipFile zipfile = prepareZip()) {
			name = zipfile.getName();
			assertNotNull("Should get some entries now", zipfile.entries());

			File file = File.createTempFile("ZipFileClose", ".test");
			try {
				FileUtils.writeStringToFile(file,
						"somedata to have something to read...");
				try (InputStream input = new FileInputStream(file)) {
					assertTrue(input.available() > 0);

					try (ZipFileCloseInputStream stream = new ZipFileCloseInputStream(input, zipfile)) {
						assertTrue(stream.available() > 0);

						List<String> lines = IOUtils.readLines(stream);
						assertEquals(1, lines.size());
					}

					try {
						zipfile.entries();
						fail("Should get exception here as the zipfile should be closed already!");
					} catch (IllegalStateException e) {
						TestHelpers.assertContains(e, "zip file closed");
					}
				}
			} finally {
				assertTrue(file.delete());
			}
		} finally {
			assertTrue(new File(name).delete());
		}

	}

	@Test
	public void testCoverMethods() throws Exception {
		String name = null;
		try (ZipFile zipfile = prepareZip()) {
			name = zipfile.getName();
			File file = File.createTempFile("ZipFileClose", ".test");
			try {
				FileUtils.writeStringToFile(file,
						"somedata to have something to read...");
				try (InputStream input = new FileInputStream(file)) {
					try (ZipFileCloseInputStream stream = new ZipFileCloseInputStream(input, zipfile)) {
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

					try {
						zipfile.entries();
						fail("Should get exception here as the zipfile should be closed already!");
					} catch (IllegalStateException e) {
						TestHelpers.assertContains(e, "zip file closed");
					}
				}
			} finally {
				assertTrue(file.delete());
			}
		} finally {
			assertTrue(new File(name).delete());
		}
	}

	@Test
	public void testReset() throws Exception {
		String name = null;
		try (ZipFile zipfile = prepareZip()) {
			name = zipfile.getName();
			File file = File.createTempFile("ZipFileClose", ".test");
			try {
				FileUtils.writeStringToFile(file,
						"somedata to have something to read...");
				try (InputStream input = new FileInputStream(file) {

					@Override
					public synchronized void reset() throws IOException {
						// just do nothing to make reset() supported here
					}
				}) {
					try (ZipFileCloseInputStream stream = new ZipFileCloseInputStream(input, zipfile)) {
						stream.reset();
					}
				}

				try {
					zipfile.entries();
					fail("Should get exception here as the zipfile should be closed already!");
				} catch (IllegalStateException e) {
					TestHelpers.assertContains(e, "zip file closed");
				}
			} finally {
				assertTrue(file.delete());
			}
		} finally {
			assertTrue(new File(name).delete());
		}
	}

	private ZipFile prepareZip() throws IOException {
		File zipfile = File.createTempFile("zipfile", ".zip");

		// assertEquals("somedata", IOUtils.toString(ZipUtils.getZipContentsRecursive(file.getAbsolutePath())));
		// ZipFile zip = new ZipFile(zipfile);
		ZipEntry entry = new ZipEntry("filename");

		try (ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(zipfile))) {
			zipout.putNextEntry(entry);
			zipout.write("somedata".getBytes());
		}

		zipfile.deleteOnExit();

		return new ZipFile(zipfile);
	}

	@Test
	public void testNullDelegate() throws IOException {
		try {
			// fail-fast with an NPE in the constructor already, not later when we do not see any more where it was coming from
			try (ZipFile prepareZip = prepareZip()) {
				try (InputStream stream = new ZipFileCloseInputStream(null, prepareZip)) {
					assertNotNull(stream);
				}
			}
			fail("Should catch exception here");
		} catch (NullPointerException e) {
			// expected here
		}
	}
}
