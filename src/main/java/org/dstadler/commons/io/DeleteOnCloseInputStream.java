package org.dstadler.commons.io;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Small wrapper InputStream which removes the underlying file as soon
 * as the InputStream is closed.
 *
 * @author dominik.stadler
 */
public class DeleteOnCloseInputStream extends FilterInputStream {
	private final static Logger logger = Logger.getLogger(DeleteOnCloseInputStream.class.getName());

	private final File file;

	public DeleteOnCloseInputStream(InputStream stream, File file) {
		super(stream);
		if(stream == null) {
			throw new NullPointerException("Delegate stream was passed null");	// NOPMD - fail early here with NullPointerException to show where the null value is coming from
		}
		this.file = file;
	}

	@Override
	public void close() throws IOException {
		super.close();

		if(!file.delete()) {
			logger.warning("Could not delete file: " + file);
		}
	}
}
