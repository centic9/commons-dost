package org.dstadler.commons.zip;

import static org.junit.jupiter.api.Assertions.*;

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
import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.Test;

public class ZipFileCloseInputStreamTest {

	@Test
	public void testZipFileCloseInputStream() throws Exception {
		String name = null;
		try (ZipFile zipfile = prepareZip()) {
			name = zipfile.getName();
			assertNotNull(zipfile.entries(), "Should get some entries now");

			File file = File.createTempFile("ZipFileClose", ".test");
			try {
				FileUtils.writeStringToFile(file,
						"somedata to have something to read...", "UTF-8");
				try (InputStream input = new FileInputStream(file)) {
					assertTrue(input.available() > 0);

					try (ZipFileCloseInputStream stream = new ZipFileCloseInputStream(input, zipfile)) {
						assertTrue(stream.available() > 0);

						List<String> lines = IOUtils.readLines(stream, "UTF-8");
						assertEquals(1, lines.size());
					}

					IllegalStateException e = assertThrows(IllegalStateException.class, zipfile::entries,
							"Should throw because the zipfile should be closed already");
					TestHelpers.assertContains(e, "zip file closed");
				}
			} finally {
				assertTrue(file.delete());
			}
		} finally {
			if(name != null) {
				assertTrue(new File(name).delete());
			}
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
						"somedata to have something to read...", "UTF-8");
				try (InputStream input = new FileInputStream(file)) {
					try (ZipFileCloseInputStream stream = new ZipFileCloseInputStream(input, zipfile)) {
						assertTrue(stream.available() > 0);

						assertTrue(file.exists());
						assertTrue(file.length() > 0);

						IOException ioe = assertThrows(IOException.class, stream::reset);
						TestHelpers.assertContains(ioe, "mark/reset not supported");

						// cover the overwritten methods
						assertEquals('s', stream.read());
						assertEquals(2, stream.read(new byte[2]));

						stream.mark(2);

						assertEquals(1, stream.skip(1));
						ioe = assertThrows(IOException.class, stream::reset);
						TestHelpers.assertContains(ioe, "mark/reset not supported");
						assertFalse(stream.markSupported());
					}

					IllegalStateException e = assertThrows(IllegalStateException.class, zipfile::entries,
							"Should throw because the zipfile should be closed already");
					TestHelpers.assertContains(e, "zip file closed");
				}
			} finally {
				assertTrue(file.delete());
			}
		} finally {
			if(name != null) {
				assertTrue(new File(name).delete());
			}
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
						"somedata to have something to read...", "UTF-8");
				try (InputStream input = new FileInputStream(file) {

					@Override
					public synchronized void reset() {
						// just do nothing to make reset() supported here
					}
				}) {
					try (ZipFileCloseInputStream stream = new ZipFileCloseInputStream(input, zipfile)) {
						stream.reset();
					}
				}

				IllegalStateException e = assertThrows(IllegalStateException.class, zipfile::entries,
						"Should throw because the zipfile should be closed already");
				TestHelpers.assertContains(e, "zip file closed");
			} finally {
				assertTrue(file.delete());
			}
		} finally {
			if(name != null) {
				assertTrue(new File(name).delete());
			}
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
		// fail-fast with an NPE in the constructor, not later when we can no longer tell where it came from
		try (ZipFile prepareZip = prepareZip()) {
			assertThrows(NullPointerException.class,
					() -> new ZipFileCloseInputStream(null, prepareZip));
		}
	}
}
