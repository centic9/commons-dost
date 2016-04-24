package org.dstadler.commons.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for handling output of searches for matching files.
 *
 * @author dominik.stadler
 */
public interface OutputHandler {
	/**
	 * Reports a file that matched.
	 *
	 * @param file The filename associated with the stream
	 * @param content An InputStream which allows to access the
	 *                content of the file.
	 * @return content The content of the found file.
	 * @throws IOException If the file cannot be read.
	 */
	boolean found(File file, InputStream content) throws IOException;
}
