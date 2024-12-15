package org.dstadler.commons.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DocumentStarterTest {

	@Test
	public void test() throws IOException {
		DocumentStarter starter = new DocumentStarter();
		assertNotNull(starter);

		assertFalse(DocumentStarter.isDisabledForTest());

		// prevent actually opening documents
		System.setProperty(DocumentStarter.PROPERTY_DOCUMENT_STARTER_DISABLE, "true");
		try {
			assertTrue(DocumentStarter.isDisabledForTest());

			runWithSpecialName(starter, "");

			runWithSpecialName(starter, ClientConstants.DBSLASH);
			runWithSpecialName(starter, ClientConstants.AMP);
			runWithSpecialName(starter, ClientConstants.LSBRA);
			runWithSpecialName(starter, ClientConstants.RSBRA);
			runWithSpecialName(starter, ClientConstants.LRBRA);
			runWithSpecialName(starter, ClientConstants.RRBRA);
			runWithSpecialName(starter, ClientConstants.PLUS);
			runWithSpecialName(starter, ClientConstants.FTICK);
			runWithSpecialName(starter, ClientConstants.BTICK);
		} finally {
			System.clearProperty(DocumentStarter.PROPERTY_DOCUMENT_STARTER_DISABLE);
		}
	}

	private void runWithSpecialName(DocumentStarter starter, String str) throws IOException {
		File tempFile = File.createTempFile(str + "document" + str, ".txt");
		try {
			starter.openFile(tempFile);
			starter.openURL("http://www.compuware.com");
			starter.openURL(new URL("http://www.compuware.com"));
		} finally {
			assertTrue(tempFile.delete());
		}

	}

	@Disabled("For local testing only, test actually opens stuff...")
	@Test
	public void testStartURLWithBlanks() throws IOException {
		File file = File.createTempFile("DocumentStarterTest-some file with blanks", ".txt");
		try {
			FileUtils.writeStringToFile(file, "test", "UTF-8");
			new DocumentStarter().openURL(file.getAbsolutePath());
		} finally {
			assertTrue(file.delete());
		}
	}
}

