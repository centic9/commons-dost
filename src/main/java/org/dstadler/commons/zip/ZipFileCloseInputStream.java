package org.dstadler.commons.zip;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

/**
 * Small wrapper InputStream which closes the underlying zipfile as soon
 * as the InputStream is closed.
 *
 * @author dominik.stadler
 */
public class ZipFileCloseInputStream extends FilterInputStream {
	private final ZipFile zipFile;

	public ZipFileCloseInputStream(InputStream stream, ZipFile second) {
		super(stream);
		if(stream == null) {
			throw new NullPointerException("Delegate stream was passed null");	// NOPMD - fail early here with NullPointerException to show where the null value is coming from
		}
		this.zipFile = second;
	}

	@Override
	public void close() throws IOException {
		super.close();

		zipFile.close();
	}
}
