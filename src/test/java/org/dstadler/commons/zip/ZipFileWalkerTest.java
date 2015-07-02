package org.dstadler.commons.zip;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.Test;

public class ZipFileWalkerTest {
	private final static Logger log = LoggerFactory.make();

	private static final String LOGLINE = "2014-04-17 13:52:12 [000000c4] info    [native]   Native watchdog feature turned off\n" +
			// add an unparsable line as well for other tests
			"2014-2j 2lkjl23 q4l;j3;4l56kj q3l46j q3;l6jq";

	@Test
	public void testWalk() throws Exception {
		File nestedZip = createNestedZip();
		try {
			ZipFileWalker walker = new ZipFileWalker(nestedZip);

			final AtomicBoolean found = new AtomicBoolean(false);
			assertFalse(walker.walk(new OutputHandler() {

				@Override
				public boolean found(File file, InputStream content) throws IOException {
					if(file.isDirectory()) {
						log.info("Directory: " + file);
						return false;
					}

					log.info("File: " + file);
					if(file.getAbsolutePath().endsWith(".log")) {
						assertEquals(LOGLINE, IOUtils.toString(content));
						found.set(true);
					}

					return false;
				}
			}));

			assertTrue(found.get());
		} finally {
			assertTrue(nestedZip.delete());
		}
	}

	@Test
	public void testStopWalk() throws Exception {
		File nestedZip = createNestedZip();
		try {
			ZipFileWalker walker = new ZipFileWalker(nestedZip);

			final AtomicBoolean found = new AtomicBoolean(false);
			assertTrue(walker.walk(new OutputHandler() {

				@Override
				public boolean found(File file, InputStream content) throws IOException {
					if(file.isDirectory()) {
						log.info("Directory: " + file);
						return true;
					}

					log.info("File: " + file);
					if(file.getAbsolutePath().endsWith(".log")) {
						assertEquals(LOGLINE, IOUtils.toString(content));
						found.set(true);
					}

					return true;
				}
			}));

			assertFalse(found.get());
		} finally {
			assertTrue(nestedZip.delete());
		}
	}

	@Test
	public void testWalkException() throws Exception {
		File nestedZip = createNestedZip();
		try {
			ZipFileWalker walker = new ZipFileWalker(nestedZip);

			try {
				walker.walk(new OutputHandler() {

					@Override
					public boolean found(File file, InputStream content) throws IOException {
						throw new IOException("testexception");
					}
				});
				fail("Should catch exception here");
			} catch (IOException e) {
				TestHelpers.assertContains(e, "testexception");
			}
		} finally {
			assertTrue(nestedZip.delete());
		}
	}

	@Test
	public void testWalkNotexisting() throws Exception {
		try {
			ZipFileWalker walker = new ZipFileWalker(new File("notexisting"));
			walker.walk(null);
			fail("Should catch exception here");
		} catch (FileNotFoundException e) {
			// expected
		}
	}

	public static File createNestedZip() throws IOException, FileNotFoundException {
		File zipfile = File.createTempFile("zipfile", ".zip");
		File zipfile2 = File.createTempFile("zipfile2", ".zip");

		try
		{
			{ // write inner zip file
				ZipEntry entry = new ZipEntry("filename.log");

				try (ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(zipfile))) {
					zipout.putNextEntry(entry);
					zipout.write(LOGLINE.getBytes());

					entry = new ZipEntry("hs_err_pid14380.log");

					zipout.putNextEntry(entry);
					zipout.write(LOGLINE.getBytes());
				}

			}

			{ // write outer zip file
				ZipEntry entry2 = new ZipEntry("nested.zip");
				try (ZipOutputStream zipout2 = new ZipOutputStream(new FileOutputStream(zipfile2))) {
					zipout2.putNextEntry(entry2);
					zipout2.write(FileUtils.readFileToByteArray(zipfile));
					zipout2.closeEntry();

					ZipEntry dirEntry = new ZipEntry("dir/");
					zipout2.putNextEntry(dirEntry);
					zipout2.closeEntry();

					ZipEntry fileEntry = new ZipEntry("dir/file");
					zipout2.putNextEntry(fileEntry);
					zipout2.closeEntry();
				}
			}

			File zipfile3 = File.createTempFile("zipfile2", ".zip");
			{ // write outer zip file
				ZipEntry entry3 = new ZipEntry("nested.zip");
				try (ZipOutputStream zipout3 = new ZipOutputStream(new FileOutputStream(zipfile3))) {
					zipout3.putNextEntry(entry3);
					zipout3.write(FileUtils.readFileToByteArray(zipfile2));
					zipout3.closeEntry();

					ZipEntry dirEntry = new ZipEntry("dir/");
					zipout3.putNextEntry(dirEntry);
					zipout3.closeEntry();

					ZipEntry fileEntry = new ZipEntry("dir/file");
					zipout3.putNextEntry(fileEntry);
					zipout3.closeEntry();
				}
			}

			return zipfile3;
		} finally {
			assertTrue(zipfile2.delete());
			assertTrue(zipfile.delete());
		}
	}
}
