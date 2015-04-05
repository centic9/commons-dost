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
		ZipFile zipfile = prepareZip();

		try {
			assertNotNull("Should get some entries now", zipfile.entries());

			File file = File.createTempFile("ZipFileClose", ".test");
			try {
				FileUtils.writeStringToFile(file,
						"somedata to have something to read...");
				InputStream input = new FileInputStream(file);

				try {
					assertTrue(input.available() > 0);

					ZipFileCloseInputStream stream = new ZipFileCloseInputStream(input, zipfile);

					assertTrue(stream.available() > 0);

					List<String> lines = IOUtils.readLines(stream);
					assertEquals(1, lines.size());

					stream.close();

					try {
						zipfile.entries();
						fail("Should get exception here as the zipfile should be closed already!");
					} catch (IllegalStateException e) {
						TestHelpers.assertContains(e, "zip file closed");
					}
				} finally {
					input.close();
				}
			} finally {
				assertTrue(file.delete());
			}
		} finally {
			zipfile.close();
			assertTrue(new File(zipfile.getName()).delete());
		}

	}

	@Test
	public void testCoverMethods() throws Exception {
		ZipFile zipfile = prepareZip();

		try {
			File file = File.createTempFile("ZipFileClose", ".test");
			try {
				FileUtils.writeStringToFile(file,
						"somedata to have something to read...");
				InputStream input = new FileInputStream(file);

				try {
					ZipFileCloseInputStream stream = new ZipFileCloseInputStream(input, zipfile);

					try {
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
					} finally {
						stream.close();
					}

					try {
						zipfile.entries();
						fail("Should get exception here as the zipfile should be closed already!");
					} catch (IllegalStateException e) {
						TestHelpers.assertContains(e, "zip file closed");
					}
				} finally {
					input.close();
				}
			} finally {
				assertTrue(file.delete());
			}
		} finally {
			zipfile.close();
			assertTrue(new File(zipfile.getName()).delete());
		}
	}

	@Test
	public void testReset() throws Exception {
		ZipFile zipfile = prepareZip();

		try {
			File file = File.createTempFile("ZipFileClose", ".test");
			try {
				FileUtils.writeStringToFile(file,
						"somedata to have something to read...");
				InputStream input = new FileInputStream(file) {

					@Override
					public synchronized void reset() throws IOException {
						// just do nothing to make reset() supported here
					}
				};

				try {
					ZipFileCloseInputStream stream = new ZipFileCloseInputStream(input, zipfile);

					stream.reset();

					stream.close();
				} finally {
					input.close();
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
			zipfile.close();
			assertTrue(new File(zipfile.getName()).delete());
		}
	}

	private ZipFile prepareZip() throws IOException {
		File zipfile = File.createTempFile("zipfile", ".zip");

		// assertEquals("somedata", IOUtils.toString(ZipUtils.getZipContentsRecursive(file.getAbsolutePath())));
		// ZipFile zip = new ZipFile(zipfile);
		ZipEntry entry = new ZipEntry("filename");

		ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(zipfile));
		try {
			zipout.putNextEntry(entry);
			zipout.write("somedata".getBytes());
		} finally {
			zipout.close();
		}

		zipfile.deleteOnExit();

		return new ZipFile(zipfile);
	}

	@Test
	public void testNullDelegate() throws IOException {
		try {
			// fail-fast with an NPE in the constructor already, not later when we do not see any more where it was coming from
			ZipFile prepareZip = prepareZip();
			try {
				assertNotNull(new ZipFileCloseInputStream(null, prepareZip));
			} finally {
				prepareZip.close();
			}
			fail("Should catch exception here");
		} catch (NullPointerException e) {
			// expected here
		}
	}
}
