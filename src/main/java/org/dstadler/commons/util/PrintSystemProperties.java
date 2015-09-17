package org.dstadler.commons.util;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple application which prints out the current Java system properties.
 *
 * @author dominik.stadler
 *
 */
public class PrintSystemProperties {
	public static void main(String[] args) throws IOException  {
		try (FileOutputStream out = new FileOutputStream("SystemProperties.log")) {
			System.getProperties().store(out, "Current System Properties");
		}
	}
}
