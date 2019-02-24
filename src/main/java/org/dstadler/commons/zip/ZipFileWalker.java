package org.dstadler.commons.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.input.CloseShieldInputStream;

/**
 * File Walker for Zip-Files which can step down into nested zip-files while
 * looking for matches.
 */
public class ZipFileWalker {
	private final File zip;

	public ZipFileWalker(File file) {
		this.zip = file;
	}

	/**
	 * Run the ZipFileWalker using the given OutputHandler
	 *
	 * @param outputHandler For every file that is found in the
	 * 		Zip-file, the method found() in the {@link OutputHandler}
	 * 		is invoked.
	 *
	 * @return true if processing was stopped because of a found file,
	 * 		false if no file was found or the {@link OutputHandler} did
	 * 		not return true on the call to found().
	 *
	 * @throws IOException Thrown if an error occurs while handling the
	 * 		Zip-file or while handling the call to found().
	 */
	public boolean walk(OutputHandler outputHandler) throws IOException {
		try (ZipFile zipFile = new ZipFile(zip)) {
			// walk all entries and look for matches
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				// first check if the file matches the name, if a name-pattern is given
				File file = new File(zip, entry.getName());
				if(outputHandler.found(file, zipFile.getInputStream(entry))) {
					return true;
				}

				// look for name or content inside zip-files as well, do it recursively to also
				// look at content of nested zip-files
				if(ZipUtils.isZip(entry.getName())) {
					if(walkRecursive(file, zipFile.getInputStream(entry), outputHandler)) {
						return true;
					}
				}
			}

			return false;
		}
	}

	@SuppressWarnings("resource")
	private boolean walkRecursive(File base, InputStream stream, OutputHandler handler) throws IOException {
		ZipInputStream zipStream = new ZipInputStream(stream);
		while(true) {
			final ZipEntry entry;
			try {
				entry = zipStream.getNextEntry();
			} catch (IOException e) {
				throw new IOException("While handling file: " + base, e);
			}

			if(entry == null) {
				break;
			}

			File file = new File(base, entry.getName());
			if(handler.found(file, new CloseShieldInputStream(zipStream))) {
				return true;
			}

			if(ZipUtils.isZip(entry.getName())) {
				if(walkRecursive(file, zipStream, handler)) {
					return true;
				}
			}
			zipStream.closeEntry();
		}

		return false;
	}
}
