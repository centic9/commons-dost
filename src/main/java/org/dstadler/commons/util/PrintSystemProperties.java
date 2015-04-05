package org.dstadler.commons.util;

import java.io.FileOutputStream;
import java.io.IOException;

public class PrintSystemProperties {

	/**
	 *
	 * @param args
	 * @author dominik.stadler
	 * @throws IOException
	 * @throws Exception
	 */
	public static void main(String[] args) throws IOException  {
		FileOutputStream out = new FileOutputStream("SystemProperties.log");
		try {
			System.getProperties().store(out, "Current System Properties");
		} finally {
			out.close();
		}
	}
}
