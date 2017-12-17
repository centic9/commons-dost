/*
 * Taken from http://www.java2s.com/Open-Source/Java-Document/IDE-Eclipse/Eclipse-plug-in-development/org/eclipse/pde/internal/runtime/logview/TailInputStream.java.htm
 */

/*
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.dstadler.commons.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * InputStream which returns the last n bytes from a stream, ensuring that
 * only full lines are returned.
 *
 */
public class TailInputStream extends InputStream {

	private RandomAccessFile fRaf;

	private long fTail;

	public TailInputStream(File file, long maxLength)
			throws IOException {
		super();
		fTail = maxLength;
		fRaf = new RandomAccessFile(file, "r"); //$NON-NLS-1$
		skipHead(file);
	}

	private void skipHead(File file) throws IOException {
		if (file.length() > fTail) {
			fRaf.seek(file.length() - fTail);
			// skip bytes until a new line to be sure we start from a beginning of valid UTF-8 character
			int c = read();
			while (c != '\n' && c != '\r' && c != -1) {
				c = read();
			}

		}
	}

	@Override
	public final int read() throws IOException {
		byte[] b = new byte[1];
		int len = fRaf.read(b, 0, 1);
		if (len < 0) {
			return len;
		}
		return b[0];
	}

	@Override
	public int read(byte[] b) throws IOException {
		return fRaf.read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return fRaf.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return fRaf.skipBytes((int)n);
	}

	@Override
	public void close() throws IOException {
		fRaf.close();
	}
}

