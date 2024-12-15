package org.dstadler.commons.zip;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.dstadler.commons.testing.TestHelpers;
import org.dstadler.commons.zip.ZipUtils.ZipFileVisitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ZipUtilsTest {
	private static final String TEST_DIRECTORY = "directory123";

	private static final File invalidDir = new File("notexists");

	@AfterEach
	public void tearDown() throws IOException {
		if (invalidDir.exists()) {
			FileUtils.deleteDirectory(invalidDir);
			fail("Directory should not exist: " + invalidDir.getAbsolutePath());
		}
	}

	/**
	 * Test method for {@link org.dstadler.commons.zip.ZipUtils#getZipContentsRecursive(java.lang.String)}.
	 */
	@Test
	public void testGetZipContentsRecursive() throws Exception {
		File file = File.createTempFile("somefile", ".txt");
		try {
			FileUtils.writeStringToFile(file, "somedata", "UTF-8");

			try (InputStream zipContents = ZipUtils.getZipContentsRecursive(file.getAbsolutePath())) {
				assertEquals("somedata", IOUtils.toString(zipContents, StandardCharsets.UTF_8));
			}
		} finally {
			assertTrue(file.exists());
			assertTrue(file.delete());
		}
	}

	@Test
	public void testGetZipContentsRecursive1() throws Exception {
		File zipfile = File.createTempFile("zipfile", ".zip");
		try {
			ZipEntry entry = new ZipEntry("filename");

			try (ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(zipfile))) {
				zipout.putNextEntry(entry);
				zipout.write("somedata".getBytes());
			}

			try (InputStream zipContents = ZipUtils.getZipContentsRecursive(zipfile.getAbsolutePath() + "!filename")) {
				assertEquals("somedata", IOUtils.toString(zipContents, StandardCharsets.UTF_8));
			}
		} finally {
			assertTrue(zipfile.exists());
			assertTrue(zipfile.delete());
		}

	}

	@Test
	public void testGetZipContentsRecursive2() throws Exception {
		File zipfile2 = createNestedZip();

		try {
    		try (InputStream zipContents = ZipUtils.getZipContentsRecursive(zipfile2.getAbsolutePath() + "!nested.zip!filename")) {
    			assertEquals("somedata",
    					IOUtils.toString(zipContents, StandardCharsets.UTF_8));
    		}
		} finally {
		    assertTrue(zipfile2.exists());
		    assertTrue(zipfile2.delete());
		}
	}

	@Test
	public void testGetZipContentsRecursiveError() throws Exception {
		try {
			ZipUtils.getZipContentsRecursive("notexistingfile");
			fail("Should catch Exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "notexistingfile");
		}

		try {
			ZipUtils.getZipContentsRecursive("notexistingfile!somefile");
			fail("Should catch Exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "notexistingfile");
		}

		try {
			ZipUtils.getZipContentsRecursive(".!somefile");
			fail("Should catch Exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, ".");
		}

		File file = File.createTempFile("ZipUtils", ".test");
		FileUtils.writeStringToFile(file, "", "UTF-8");
		assertEquals(0, file.length());
		try {
			try {
				ZipUtils.getZipContentsRecursive(file.getAbsolutePath() + "!somefile");
				fail("Should catch Exception here");
			} catch (IOException e) {
				TestHelpers.assertContains(e, file.getAbsolutePath());
			}
		} finally {
			assertTrue(file.delete());
		}
	}

	@Test
	public void testGetZipContentsDirectoryInsteadOfFile() {
		// make sure we have a directory
		assertTrue((new File(TEST_DIRECTORY).exists() && new File(TEST_DIRECTORY).isDirectory()) ||
				new File(TEST_DIRECTORY).mkdirs());

		try {
			ZipUtils.getZipContentsRecursive(TEST_DIRECTORY);
		} catch (IOException e) {
			TestHelpers.assertContains(e, TEST_DIRECTORY);
		} finally {
			assertTrue(new File(TEST_DIRECTORY).delete());
		}
	}

	@Test
	public void testGetZipContentsNotexistingInnerZipFile() throws Exception {
		File zipfile = createNestedZip();

		try {
			ZipUtils.getZipContentsRecursive(zipfile.getAbsolutePath() + "!zipfileDiff.zip!filename");
			fail("Should catch exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "zipfileDiff.zip");
        } finally {
            assertTrue(zipfile.exists());
            assertTrue(zipfile.delete());
        }
	}

	@Test
	public void testGetZipContentsInvalidZipFile() {
		try {
			ZipUtils.getZipContentsRecursive("zipfileNotExist.zip!filename");
			fail("Should catch exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "zipfileNotExist.zip");
		}
	}

	@Test
	public void testIsZip() {
		assertTrue(ZipUtils.isZip("file.zip"));
		assertTrue(ZipUtils.isZip("file.Zip"));
		assertTrue(ZipUtils.isZip("file.ZIP"));
		assertTrue(ZipUtils.isZip("file.ziP"));

		assertTrue(ZipUtils.isZip("file.war"));
		assertTrue(ZipUtils.isZip("file.ear"));
		assertTrue(ZipUtils.isZip("file.jar"));
		assertTrue(ZipUtils.isZip("file.aar"));
		assertTrue(ZipUtils.isZip("file.jmod"));

		assertFalse(ZipUtils.isZip("file.some"));
		assertFalse(ZipUtils.isZip("file.zip.gz"));
		assertFalse(ZipUtils.isZip("file.somezip"));
		assertFalse(ZipUtils.isZip("file.nzip"));
		assertFalse(ZipUtils.isZip(""));
		assertFalse(ZipUtils.isZip(null));
	}

	@Test
	public void testFindZip() throws Exception {
		File zipfile = createNestedZip();

		try {
    		ArrayList<String> results = new ArrayList<>();
    		try (InputStream zipInput = new FileInputStream(zipfile)) {
    			ZipUtils.findZip("zipfile", zipInput, FileFilterUtils.falseFileFilter(), results);
    		}
    		assertEquals(0, results.size(), "look for files with no-accept filter and expect to accept none");

    		results.clear();
    		try (InputStream zipInput = new FileInputStream(zipfile)) {
    			ZipUtils.findZip("zipfile", zipInput, FileFilterUtils.trueFileFilter(), results);
    		}
    		assertEquals(
    				6, results.size(), "look for files with all-accept filter and expect one entry for the nested zip and one entry for the deeply nested file as well as a dir and a file underneath");

    		results.clear();
    		try (InputStream stream = new ExceptionInputStream(new IOException("testexception"))) {
    			ZipUtils.findZip("myownzipfile", stream,
    					FileFilterUtils.trueFileFilter(), results);
    		} catch (IOException e) {
    			TestHelpers.assertContains(e.getCause(), "testexception");
    			TestHelpers.assertContains(e, "myownzipfile");
    		}
    		assertEquals(0, results.size(), "no files when having exception");

    		results.clear();
    		try (InputStream stream = new ExceptionInputStream(new IllegalArgumentException("testexception"))) {
    			ZipUtils.findZip("myownzipfile", stream,
    					FileFilterUtils.trueFileFilter(), results);
    		} catch (IOException e) {
    			TestHelpers.assertContains(e.getCause(), "testexception");
    			TestHelpers.assertContains(e, "myownzipfile");
    		}
    		assertEquals(0, results.size(), "no files when having exception");
		} finally {
		    assertTrue(zipfile.exists());
		    assertTrue(zipfile.delete());
		}
	}

	private File createNestedZip() throws IOException {
		File zipfile = File.createTempFile("zipfile", ".zip");

		try
		{
			{ // write inner zip file
				ZipEntry entry = new ZipEntry("filename");

				try (ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(zipfile))) {
					zipout.putNextEntry(entry);
					zipout.write("somedata".getBytes());
				}
			}

			File zipfile2 = File.createTempFile("zipfile2", ".zip");
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

					ZipEntry fileEntry2 = new ZipEntry("dir/file2");
					zipout2.putNextEntry(fileEntry2);
					zipout2.write("testcontent".getBytes());
					zipout2.closeEntry();

					ZipEntry fileEntry3 = new ZipEntry("subdir/subdir/file3");
					zipout2.putNextEntry(fileEntry3);
					zipout2.write("testcontent".getBytes());
					zipout2.closeEntry();
				}
			}
			return zipfile2;
		} finally {
			assertTrue(zipfile.delete());
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(ZipUtils.class);
	}

	private static final class ExceptionInputStream extends InputStream {

		IOException ioException;
		RuntimeException runtimeException;

		public ExceptionInputStream(IOException exception) {
			this.ioException = exception;
		}

		public ExceptionInputStream(RuntimeException exception) {
			this.runtimeException = exception;
		}

		@Override
		public int read() throws IOException {
			if (ioException != null) {
				throw ioException;
			}

			assertNotNull(runtimeException);
			throw runtimeException;
		}
	}

	@Test
	public void testWithDifferentLogLevel() {
		TestHelpers.runTestWithDifferentLogLevel(() -> {
            try {
                testGetZipContentsRecursive2();
                testGetZipStringContentsRecursive2();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, ZipUtils.class.getName(), Level.FINE);
	}

	@Test
	public void testGetZipStringContentsRecursive() throws Exception {
		File file = File.createTempFile("somefile", ".txt");
		try {
			FileUtils.writeStringToFile(file, "somedata", "UTF-8");

			assertEquals("somedata", ZipUtils.getZipStringContentsRecursive(file.getAbsolutePath()));
		} finally {
			assertTrue(file.delete());
		}
	}

	@Test
	public void testGetZipStringContentsRecursiveEmptyFile() throws Exception {
		File file = File.createTempFile("somefile", ".txt");
		try {
			assertEquals("", ZipUtils.getZipStringContentsRecursive(file.getAbsolutePath()));
		} finally {
			assertTrue(file.delete());
		}
	}

	@Test
	public void testGetZipStringContentsRecursive1() throws Exception {
		File zipfile = File.createTempFile("zipfile", ".zip");
		try {
			ZipEntry entry = new ZipEntry("filename");

			try (ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(zipfile))) {
				zipout.putNextEntry(entry);
				zipout.write("somedata".getBytes());
			}

			assertEquals("somedata", ZipUtils.getZipStringContentsRecursive(zipfile.getAbsolutePath() + "!filename"));
		} finally {
			assertTrue(zipfile.delete());
		}
	}

	@Test
	public void testGetZipStringContentsRecursive2() throws Exception {
		File zipfile2 = createNestedZip();

		try {
			assertEquals("somedata",
				ZipUtils.getZipStringContentsRecursive(zipfile2.getAbsolutePath() + "!nested.zip!filename"));
		} finally {
		    assertTrue(zipfile2.exists());
			assertTrue(zipfile2.delete());
		}
	}

	@Test
	public void testGetZipStringContentsRecursiveError() throws Exception {
		try {
			ZipUtils.getZipStringContentsRecursive("notexistingfile");
			fail("Should catch Exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "notexistingfile");
		}

		try {
			ZipUtils.getZipStringContentsRecursive("notexistingfile!somefile");
			fail("Should catch Exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "notexistingfile");
		}

		try {
			ZipUtils.getZipStringContentsRecursive(".!somefile");
			fail("Should catch Exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, ".");
		}

		File file = File.createTempFile("ZipUtils", ".test");
		FileUtils.writeStringToFile(file, "", "UTF-8");
		assertEquals(0, file.length());
		try {
			try {
				ZipUtils.getZipStringContentsRecursive(file.getAbsolutePath() + "!somefile");
				fail("Should catch Exception here");
			} catch (IOException e) {
				TestHelpers.assertContains(e, file.getAbsolutePath());
			}
		} finally {
			assertTrue(file.delete());
		}
	}

	@Test
	public void testGetZipStringContentsDirectoryInsteadOfFile() {
		// make sure we have a directory
		assertTrue((new File(TEST_DIRECTORY).exists() && new File(TEST_DIRECTORY).isDirectory()) ||
				new File(TEST_DIRECTORY).mkdirs());

		try {
			ZipUtils.getZipStringContentsRecursive(TEST_DIRECTORY);
		} catch (IOException e) {
			TestHelpers.assertContains(e, TEST_DIRECTORY);
		} finally {
			assertTrue(new File(TEST_DIRECTORY).delete());
		}
	}

	@Test
	public void testGetZipStringContentsNotexistingInnerZipFile() throws Exception {
		File zipfile = createNestedZip();

		try {
			ZipUtils.getZipStringContentsRecursive(zipfile.getAbsolutePath() + "!zipfileDiff.zip!filename");
			fail("Should catch exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "zipfileDiff.zip");
		} finally {
		    assertTrue(zipfile.exists());
			assertTrue(zipfile.delete());
		}
	}

	@Test
	public void testGetZipStringContentsInvalidZipFile() {
		try {
			ZipUtils.getZipStringContentsRecursive("zipfileNotExist.zip!filename");
			fail("Should catch exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "zipfileNotExist.zip");
		}
	}

	@Disabled("Local test, will not work in other places")
	@Test
	public void testIcefaces() throws Exception {
		List<String> r = new ArrayList<>();
		try (InputStream zipInput = new FileInputStream("C:\\data\\easyTravel\\ThirdPartyLibraries\\IceFaces\\ICEfaces-2.0.0-bin\\icefaces\\lib\\jsf-impl.jar")) {
			ZipUtils.findZip("C:\\data\\easyTravel\\ThirdPartyLibraries\\IceFaces\\ICEfaces-2.0.0-bin\\icefaces\\lib\\jsf-impl.jar",
					zipInput,
					t -> {
                        System.out.println("Check " + t);
                        return t.getPath().matches(".*jsf.js");
                    }, r);
			assertEquals(1, r.size(), "List: " + r);
		}
	}

	@Test
	public void testIsFileInZip() {
		assertFalse(ZipUtils.isFileInZip(null));
		assertFalse(ZipUtils.isFileInZip(""));
		assertFalse(ZipUtils.isFileInZip("test.xyz"));
		assertFalse(ZipUtils.isFileInZip("test.zip"));

		assertTrue(ZipUtils.isFileInZip("test.zip!"));
		assertTrue(ZipUtils.isFileInZip("test.zip!somefile"));
		assertTrue(ZipUtils.isFileInZip("test.zip!test2.zip"));
	}

	@Test
	public void testExtractZip() throws IOException {
		File zipfile2 = createNestedZip();

		try {
    		try {
				assertFalse(invalidDir.exists(),
						"Directory should not exist: " + invalidDir.getAbsolutePath());

				ZipUtils.extractZip(zipfile2, invalidDir);
    			fail("Should catch exception here");
    		} catch (IOException e) {
    			TestHelpers.assertContains(e, invalidDir.getName(), "does not exist");
    		}

    		File toDir = File.createTempFile("toDir", "");
    		assertTrue(toDir.delete());
    		assertTrue(toDir.mkdir());

    		try {
        		ZipUtils.extractZip(zipfile2, toDir);

        		assertTrue(new File(toDir, "nested.zip").exists(),
        				"File not found: " + new File(toDir, "zpifile.zip"));
        		assertTrue(new File(toDir, "dir").exists(),
        				"Dir not found: " + new File(toDir, "dir"));
        		assertTrue(new File(toDir, "dir/file").exists(),
        				"Dir/File not found: " + new File(toDir, "dir/file"));

        		ZipUtils.extractZip(zipfile2, toDir);

        		assertTrue(new File(toDir, "nested.zip").exists(),
        				"File not found: " + new File(toDir, "zpifile.zip"));
    		} finally {
    		    FileUtils.deleteDirectory(toDir);
    		}
		} finally {
		    assertTrue(zipfile2.exists());
		    assertTrue(zipfile2.delete());
		}
	}

	@Test
	public void testExtractNonExistingZip() throws IOException {
		File zipfile = new File("nonexistingfile.zip");

		File toDir = File.createTempFile("toDir", "");
		assertTrue(toDir.delete());
		assertTrue(toDir.mkdir());

		try {
			ZipUtils.extractZip(zipfile, toDir);
			fail("Should fail because file does not exist");
		} catch (FileNotFoundException | NoSuchFileException e) {
			TestHelpers.assertContains(e, "nonexistingfile.zip");
		} finally {
		    FileUtils.deleteDirectory(toDir);
		}
	}

	@Test
	public void testExtractInvalidZip() throws IOException {
		File zipfile = new File("nonexistingfile.zip");
		try {
			FileUtils.writeByteArrayToFile(zipfile, new byte[] { 1,2,3,4});

			File toDir = File.createTempFile("toDir", "");
			assertTrue(toDir.delete());
			assertTrue(toDir.mkdir());

			try {
				ZipUtils.extractZip(zipfile, toDir);
				fail("Should fail because file is invalid");
			} catch (IOException e) {
				TestHelpers.assertContains(e, "nonexistingfile.zip");
			} finally {
			    FileUtils.deleteDirectory(toDir);
			}
		} finally {
			assertTrue(zipfile.exists());
			assertTrue(zipfile.delete());
		}
	}

	@Test
	public void testExtractZipFromStream() throws IOException {
		File zipfile2 = createNestedZip();

		try (InputStream stream = new FileInputStream(zipfile2)){
    		try {
				assertFalse(invalidDir.exists(),
						"Directory should not exist: " + invalidDir.getAbsolutePath());

				ZipUtils.extractZip(stream, invalidDir);
    			fail("Should catch exception here");
    		} catch (IOException e) {
    			TestHelpers.assertContains(e, invalidDir.getName(), "does not exist");
    		}

    		File toDir = File.createTempFile("toDir", "");
    		assertTrue(toDir.delete());
    		assertTrue(toDir.mkdir());

    		try {
        		ZipUtils.extractZip(stream, toDir);

        		assertTrue(new File(toDir, "nested.zip").exists(),
        				"File not found: " + new File(toDir, "zpifile.zip"));
        		assertTrue(new File(toDir, "dir").exists(),
        				"Dir not found: " + new File(toDir, "dir"));
        		assertTrue(new File(toDir, "dir/file").exists(),
        				"Dir/File not found: " + new File(toDir, "dir/file"));
    		} finally {
    		    FileUtils.deleteDirectory(toDir);
    		}
		} finally {
		    assertTrue(zipfile2.exists());
		    assertTrue(zipfile2.delete());
		}
	}

	@Test
	public void testReplaceInZipFailed() {
		try {
			ZipUtils.replaceInZip(null, "somedata", null);
			fail("Should catch exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "inside a ZIP file");
		}

		try {
			ZipUtils.replaceInZip("somefile", "somedata", null);
			fail("Should catch exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "inside a ZIP file");
		}

		try {
			ZipUtils.replaceInZip("somefile!somefile", "somedata", null);
			fail("Should catch exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "inside a ZIP file");
		}

		try {
			ZipUtils.replaceInZip("somezip.zip!somefile", "somedata", null);
			fail("Should catch exception here");
		} catch (IOException e) {
			// error is different between JDK 6 and 7
			assertTrue(e.getMessage().contains("ZIP file must have at least one entry")
					|| e.getMessage().contains("somezip.zip"),
					"Had: " + e);
		}
	}

	@Test
	public void testReplaceInZipReplace() throws IOException {
		File zipfile = createNestedZip();

		try {
    		ZipUtils.replaceInZip(zipfile.getAbsolutePath() + "!nested.zip", "somenewdata", null);

    		try (ZipFile checkZip = new ZipFile(zipfile)) {
    			assertNotNull(checkZip.getEntry("nested.zip"));
    			try (InputStream stream = checkZip.getInputStream(checkZip.getEntry("nested.zip"))) {
    				assertEquals("somenewdata", IOUtils.toString(stream, StandardCharsets.UTF_8));
    			}
    		}
		} finally {
		    assertTrue(zipfile.exists());
		    assertTrue(zipfile.delete());
		}
	}

	@SuppressWarnings("UnnecessaryUnicodeEscape")
	@Test
	public void testReplaceInZipReplaceEncoding() throws IOException {
		File zipfile = createNestedZip();

		try {
    		ZipUtils.replaceInZip(zipfile.getAbsolutePath() + "!nested.zip", "somenewdata\u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF", "ISO-8859-1");

    		try (ZipFile checkZip = new ZipFile(zipfile)) {
    			assertNotNull(checkZip.getEntry("nested.zip"));

    			try (InputStream stream = checkZip.getInputStream(checkZip.getEntry("nested.zip"))) {
    				String data = IOUtils.toString(stream, StandardCharsets.UTF_8);
					assertNotEquals("somenewdata\u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF", data, "Should be different without encoding, expected: somenewdata\u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF\nhad: " + data);
    			}

    			try (InputStream stream = checkZip.getInputStream(checkZip.getEntry("nested.zip"))) {
    				assertEquals("somenewdata\u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF",
							IOUtils.toString(stream, StandardCharsets.ISO_8859_1),
							"Should be equal with same encoding");
    			}
    		}
		} finally {
		    assertTrue(zipfile.exists());
		    assertTrue(zipfile.delete());
		}
	}

	@Test
	public void testReplaceInZipAdd() throws IOException {
		File zipfile = createNestedZip();

		try {
    		ZipUtils.replaceInZip(zipfile.getAbsolutePath() + "!newfile.zip", "somemorenewdata", null);

    		try (ZipFile checkZip = new ZipFile(zipfile)) {
    			assertNotNull(checkZip.getEntry("newfile.zip"));
    			try (InputStream stream = checkZip.getInputStream(checkZip.getEntry("newfile.zip"))) {
    				assertEquals("somemorenewdata", IOUtils.toString(stream, StandardCharsets.UTF_8));
    			}
    		}
        } finally {
            assertTrue(zipfile.exists());
            assertTrue(zipfile.delete());
        }
	}

	@Test
	public void testZipFileVisitor() throws IOException {
		File zipfile = createNestedZip();

		try {
    		final AtomicBoolean found = new AtomicBoolean(false);
    		ZipFileVisitor visitor = new ZipFileVisitor() {

    			@Override
    			public void visit(ZipEntry entry, InputStream data) {
    				found.set(true);
    			}
    		};

    		try (InputStream zipFile2 = new FileInputStream(zipfile)) {
    			visitor.walk(zipFile2);
    		}

    		assertTrue(found.get(), "Expect to have found at least some files");
        } finally {
            assertTrue(zipfile.exists());
            assertTrue(zipfile.delete());
        }
	}

	@Test
	public void testZipFileVisitorFails() throws IOException {
		File zipfile = createNestedZip();

		try {
    		final AtomicBoolean found = new AtomicBoolean(false);
    		ZipFileVisitor visitor = new ZipFileVisitor() {

    			@Override
    			public void visit(ZipEntry entry, InputStream data) {
    				found.set(true);
    			}
    		};

    		try (InputStream zipFile2 = new FileInputStream(zipfile) {

    			@Override
    			public int read(byte[] b, int off, int len) throws IOException {
    				throw new IOException("testexception");
    			}
    		}) {
    			visitor.walk(zipFile2);
    			fail("Should catch IOException here");
    		} catch (IOException e) {
    			TestHelpers.assertContains(e, "testexception");
    		}

    		assertFalse(found.get(), "Expect to have found at least some files");
        } finally {
            assertTrue(zipfile.exists());
            assertTrue(zipfile.delete());
        }
	}
}
