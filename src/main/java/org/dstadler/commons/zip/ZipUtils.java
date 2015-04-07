package org.dstadler.commons.zip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.dstadler.commons.io.DeleteOnCloseInputStream;


/**
 *
 * @author dominik.stadler
 */
public class ZipUtils {
    private final static Logger logger = Logger.getLogger(ZipUtils.class.getName());

	private static final char ZIP_DELIMITER = '!';

	/**
	 * Extensions for known ZIP files, need to be in lowercase to match below!
	 */
	private static final String[] ZIP_EXTENSIONS = {
		// normal Zip
		".zip",

		// Java archives
		".jar", ".war", ".ear",

		// Axis2 Services archives
		".aar",

		// Jenkins/Hudson plugin bundle
		".hpi",

		// Microsoft Office files
		".xlsx", ".docx", ".pptx"
	};

	/**
	 * Determines if the file has an extension known to be a ZIP file,
	 * currently this includes .zip, .jar, .war, .ear, .aar
	 *
	 * @param t
	 * @return
	 */
	public static boolean isZip(String t) {
		if (t == null) {
			return false;
		}

		String tl = t.toLowerCase();
		for (String element : ZIP_EXTENSIONS) {
			if (tl.endsWith(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the string denotes a file inside a ZIP file using the notation
	 * used for getZipContentsRecursive().
	 *
	 * @param t
	 * @return
	 * @author dominik.stadler
	 */
	public static boolean isFileInZip(String t) {
		if (t == null) {
			return false;
		}

		for (String element : ZIP_EXTENSIONS) {
			if (t.toLowerCase().contains(element + ZIP_DELIMITER)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Looks in the ZIP file available via zipInput for files matching the provided file-filter,
	 * recursing into sub-ZIP files.
	 *
	 * @param zipName
	 * @param zipInput
	 * @param searchFilter
	 * @param results
	 *
	 * @throws IOException
	 *         If the ZIP file cannot be read, e.g. if it is corrupted.
	 */
	public static void findZip(String zipName, InputStream zipInput, FileFilter searchFilter, List<String> results)
			throws IOException {
		ZipInputStream zin = new ZipInputStream(zipInput);
		while (true) {
			final ZipEntry en;
			try {
				en = zin.getNextEntry();
			} catch (IOException e) {
				throw new IOException("While handling file " + zipName, e);
			} catch (IllegalArgumentException e) {
				throw new IOException("While handling file " + zipName, e);
			}
			if(en == null) {
				break;
			}
			if (searchFilter.accept(new File(en.getName()))) {
				results.add(zipName + ZIP_DELIMITER + en);
			}
			if (ZipUtils.isZip(en.getName())) {
				findZip(zipName + ZIP_DELIMITER + en, zin, searchFilter, results);
			}
		}
	}

	/**
	 * Get a stream of the noted file which potentially resides inside ZIP files. An exclamation mark '!'
	 * denotes a zip-entry. ZIP files can be nested inside one another.
	 *
	 * e.g.
	 *
	 * c:\temp\test.zip!sample.zip!my.zip!somefile.txt
	 *
	 * If there is no exclamation mark contained in the file-parameter, an input stream to this file is
	 * returned directly.
	 *
	 * means that there is a zip file c:\temp\test.zip which contains a file "sample.zip", which itself
	 * contains a file "my.zip" which finally contains a file "somefile.txt"
	 *
	 * @param file The name of the file to read, files inside zip files are denoted with '!'.
	 *
	 * @return A stream that points to the file inside the ZIP file.
	 *
	 * @throws IOException If the file cannot be found or an error occurs while opening the file.
	 */
	@SuppressWarnings("resource")
	public static InputStream getZipContentsRecursive(final String file) throws IOException {
		// return local file directly
		int pos = file.indexOf('!');
		if (pos == -1) {
			if (!new File(file).exists()) {
				throw new IOException("File " + file + " does not exist");
			}

			try {
				return new FileInputStream(file);
			} catch (IOException e) {
				// filter out locked errors
				if (e.getMessage().contains("because another process has locked")) {
					logger.warning("Could not read file: " + file + " because it is locked.");

					return new ByteArrayInputStream(new byte[] {});
				}

				throw e;
			}
		}

		String zip = file.substring(0, pos);
		String subfile = file.substring(pos + 1);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Trying to read zipfile: " + zip + " subfile: " + subfile);
		}

		// open original zip
		if (!new File(zip).exists() || !new File(zip).isFile() || !new File(zip).canRead() || new File(zip).length() == 0) {
			throw new IOException("ZIP file: " + zip + " does not exist or is empty or not a readable file.");
		}

		ZipFile zipfile = new ZipFile(zip);
		// is the target file in yet another ZIP file?
		pos = subfile.indexOf('!');
		if (pos != -1) {
			// find out first ZIP file and remainder
			String remainder = subfile.substring(pos + 1);

			File subzipfile = File.createTempFile("ZipUtils", ".zip");
			try {
				readToTemporaryFile(pos, zip, subfile, zipfile, subzipfile);

				// start another recursion with the temporary file and the remainder
				return new DeleteOnCloseInputStream(
						new ZipFileCloseInputStream(getZipContentsRecursive(subzipfile.getAbsolutePath() + ZIP_DELIMITER + remainder), zipfile),
						subzipfile);
			} catch (IOException e) {
				// need to close the zipfile here as we do not put it into a ZipFileCloseInputStream
				zipfile.close();

				throw e;
			} finally {
				if (!subzipfile.delete()) {
					logger.warning("Could not delete file " + subzipfile);
				}
			}
		}

		ZipEntry entry = zipfile.getEntry(subfile);
		return new ZipFileCloseInputStream(zipfile.getInputStream(entry), zipfile);
	}

	private static void readToTemporaryFile(int pos, String zip, String subfile, ZipFile zipfile, File subzipfile)
			throws IOException {
		// open the inner-zip
		ZipEntry entry = openInnerZip(pos, zip, subfile, zipfile);

		// read the zipfile into a temporary file
		try (InputStream zipstr = zipfile.getInputStream(entry)) {
			FileUtils.copyInputStreamToFile(zipstr, subzipfile);
		}
	}

	private static ZipEntry openInnerZip(int pos, String zip, String subfile, ZipFile zipfile) throws IOException {
		String zipInner = subfile.substring(0, pos);
		ZipEntry entry = zipfile.getEntry(zipInner);
		if (entry == null) {
			throw new IOException("Could not read inner ZIP file: '" + zipInner + "' from ZIP file '" + zip + "'");
		}
		return entry;
	}

	/**
	 * Get the text-contents of the noted file. An exclamation mark '!' denotes a zip-entry. ZIP files can
	 * be nested inside one another.
	 *
	 * e.g.
	 *
	 * c:\temp\test.zip!sample.zip!my.zip!somefile.txt
	 *
	 * If there is no exclamation mark contained in the file-parameter, an input stream to this file is
	 * returned directly.
	 *
	 * means that there is a zip file c:\temp\test.zip which contains a file "sample.zip", which itself
	 * contains a file "my.zip" which finally contains a file "somefile.txt"
	 *
	 * @param file The name of the file to read, files inside zip files are denoted with '!'.
	 *
	 * @return The text-contents of the file
	 *
	 * @throws IOException If the file cannot be found or an error occurs while opening the file.
	 */
	public static String getZipStringContentsRecursive(final String file) throws IOException {
		// return local file directly
		int pos = file.indexOf('!');
		if (pos == -1) {
			if (!new File(file).exists()) {
				throw new IOException("File " + file + " does not exist");
			}

			try {
				try (InputStream str = new FileInputStream(file)) {
					if (str.available() > 0) {
						return IOUtils.toString(str);
					}

					return "";
				}
			} catch (IOException e) {
				// filter out locked errors
				if (e.getMessage().contains("because another process has locked")) {
					logger.warning("Could not read file: " + file + " because it is locked.");

					return "";
				}

				throw e;
			}
		}

		String zip = file.substring(0, pos);
		String subfile = file.substring(pos + 1);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Trying to read zipfile: " + zip + " subfile: " + subfile);
		}

		// open original zip
		if (!new File(zip).exists() || !new File(zip).isFile() || !new File(zip).canRead() || new File(zip).length() == 0) {
			throw new IOException("ZIP file: " + zip + " does not exist or is empty or not a readable file.");
		}

		try (ZipFile zipfile = new ZipFile(zip)) {
			// is the target file in yet another ZIP file?
			pos = subfile.indexOf('!');
			if (pos != -1) {
				// find out first ZIP file and remainder
				String remainder = subfile.substring(pos + 1);

				File subzipfile = File.createTempFile("SearchZip", ".zip");
				try {
					readToTemporaryFile(pos, zip, subfile, zipfile, subzipfile);

					// start another recursion with the temporary file and the remainder
					return getZipStringContentsRecursive(subzipfile.getAbsolutePath() + ZIP_DELIMITER + remainder);
				} finally {
					if (!subzipfile.delete()) {
						logger.warning("Could not delete file " + subzipfile);
					}
				}
			}

			ZipEntry entry = zipfile.getEntry(subfile);

			try (InputStream str = zipfile.getInputStream(entry)) {
				if (str.available() > 0) {
					return IOUtils.toString(str);
				}

				return "";
			}
		}
	}

	/**
	 * Extracts all files in the specified ZIP file and stores them in the
	 * denoted directory. The directory needs to exist before running this method.
	 *
	 * Note: nested ZIP files are not extracted here.
	 *
	 * @param zip
	 * @param toDir Target directory, should already exist.
	 *
	 * @throws IOException
	 *
	 * @author dominik.stadler
	 */
	public static final void extractZip(File zip, File toDir) throws IOException{
		if(!toDir.exists()) {
			throw new IOException("Directory '" + toDir + "' does not exist.");
		}

		try (ZipFile zipFile = new ZipFile(zip)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				if (entry.isDirectory()) {
					// Assume directories are stored parents first then children.
					logger.info("Extracting directory: " + entry.getName());
					// This is not robust, just for demonstration purposes.
					if(!(new File(toDir, entry.getName())).mkdir()) {
						logger.warning("Could not create directory " + new File(toDir, entry.getName()));
					}
					continue;
				}

				logger.info("Extracting file: " + entry.getName());
				try (InputStream inputStream = zipFile.getInputStream(entry)) {
					try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(toDir,
							entry.getName())))) {
						IOUtils.copy(inputStream, outputStream);
					}
				}
			}
		}
	}

	/**
	 * Replace the file denoted by the zipFile with the provided data. The zipFile specifies
	 * both the zip and the file inside the zip using '!' as separator.
	 *
	 * @param zipFile
	 * @param data
	 * @param encoding
	 * @throws IOException
	 * @author dominik.stadler
	 */
	public static void replaceInZip(String zipFile, String data, String encoding) throws IOException {
		if(zipFile == null || !isFileInZip(zipFile)) {
			throw new IOException("Parameter should specify a file inside a ZIP file, but had: " + zipFile);
		}

		File zip = new File(zipFile.substring(0, zipFile.indexOf(ZIP_DELIMITER)));
		String zipOut = zipFile.substring(zipFile.indexOf(ZIP_DELIMITER)+1);

		logger.info("Updating containing Zip " + zip + " to " + zipOut);

		// replace in zip
		ZipUtils.replaceInZip(zip, zipOut, data, encoding);
	}
	/**
	 * Replaces the specified file in the provided ZIP file with the
	 * provided content.
	 *
	 * @param zip
	 * @param file
	 * @param data
	 * @param encoding
	 * @throws IOException
	 * @author dominik.stadler
	 */
	public static void replaceInZip(File zip, String file, String data, String encoding) throws IOException {
		// open the output side
		File zipOutFile = File.createTempFile("ZipReplace", ".zip");
		try {
			FileOutputStream fos = new FileOutputStream(zipOutFile);
			try (ZipOutputStream zos = new ZipOutputStream(fos)) {
				// open the input side
				try (ZipFile zipFile = new ZipFile(zip)) {
					boolean found = false;

					// walk all entries and copy them into the new file
					Enumeration<? extends ZipEntry> entries = zipFile.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();

						try {
							if (entry.getName().equals(file)) {
								zos.putNextEntry(new ZipEntry(entry.getName()));
								IOUtils.write(data, zos, encoding);
								found = true;
							} else {
								zos.putNextEntry(entry);
								IOUtils.copy(zipFile.getInputStream(entry), zos);
							}
						} finally {
							zos.closeEntry();
						}
					}

					if(!found) {
						zos.putNextEntry(new ZipEntry(file));
						try {
							IOUtils.write(data, zos);
						} finally {
							zos.closeEntry();
						}
					}
				}
			}

			// copy over the data
			FileUtils.copyFile(zipOutFile, zip);
		} finally {
			if(!zipOutFile.delete()) {
				throw new IOException("Error deleting file: " + zipOutFile);
			}
		}
	}

	/**
		new ZipUtils.ZipFileVisitor() {

			@Override
			public void visit(ZipEntry entry, InputStream data) throws IOException {
				// process file data
			}
		}.walk(new ByteArrayInputStream(zip));
	 *
	 * @author dominik.stadler
	 */
	public static abstract class ZipFileVisitor {
		public void walk(InputStream zipFile) throws IOException {
			try (ZipInputStream stream = new ZipInputStream(zipFile)) {
	            // while there are entries I process them
		        while (true)
		        {
		        	ZipEntry entry = stream.getNextEntry();
		        	if(entry == null) {
		        		break;
		        	}

		        	visit(entry, stream);
		        }
			}
		}

		public abstract void visit(ZipEntry entry, InputStream data) throws IOException;
	}
}
