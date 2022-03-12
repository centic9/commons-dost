package org.dstadler.commons.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * A small wrapper around BufferedReader which supports a "peek"
 * of the next line without actually removing it from the lines
 * returned by "readLine()".
 *
 * The peeked line is still returned on the next readLine() call.
 *
 * This allows to look at the next line without removing it
 * from the list of lines.
 */
public class BufferedReaderWithPeek implements AutoCloseable {

	private final BufferedReader delegate;

	private String peekedLine = null;

	public BufferedReaderWithPeek(String file) throws FileNotFoundException {
		this(new BufferedReader(new FileReader(file)));
	}

	public BufferedReaderWithPeek(BufferedReader delegate) {
		this.delegate = delegate;
	}

	/**
	 * Return the next line without taking it off of the BufferedReader
	 * <p>
	 * Calling this method multiple times without "readLine()" returns
	 * the same line.
	 *
	 * @return The line that would be returned by readLine(). Multiple calls will return the same line.
	 * Only calling readLine() will advance the reader.
	 * @throws java.io.IOException If reading from the reader fails
	 */
	public String peekLine() throws IOException {
		if (peekedLine == null) {
			peekedLine = delegate.readLine();
		}

		return peekedLine;
	}

	/**
	 * Returns the next line from the BufferedReader, may be the line
	 * that was previously returned via #peekLine().
	 *
	 * @see java.io.BufferedReader#readLine() for details
	 */
	public String readLine() throws IOException {
		if (peekedLine != null) {
			String line = peekedLine;
			peekedLine = null;
			return line;
		}

		return delegate.readLine();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
