package org.dstadler.commons.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for handling output of the Search.
 *
 * @author dominik.stadler
 */
public interface OutputHandler {
	/**
	 * Reports a file that matched.
	 *
	 * @param file
	 * @return true if processing can be stopped, false otherwise
	 * @throws IOException
	 */
	public boolean found(File file, InputStream content) throws IOException;
}
