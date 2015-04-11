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
		try (FileOutputStream out = new FileOutputStream("SystemProperties.log")) {
			System.getProperties().store(out, "Current System Properties");
		}
	}
}
