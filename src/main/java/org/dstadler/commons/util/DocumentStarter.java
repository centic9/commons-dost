package org.dstadler.commons.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 * The default implementation of the web browser instance.
 */
public class DocumentStarter {
	private static final Logger logger = Logger.getLogger(DocumentStarter.class.getName());

	/**
	 * Set this system property in unit tests to not have DocumentStarter open any documents
	 */
	public static final String PROPERTY_DOCUMENT_STARTER_DISABLE = "org.dstadler.commons.util.DocumentStarter.disable";

	public void openFile(File file) {
		try {
			String fileString = file.toString();
			// path containing special chars
			if (checkForBadFileName(fileString)) {
				openURL(file.getCanonicalPath());
			} else {
				// normal path including spaces
				logger.info("Opening document " + file.toURI().toURL());
				openURL(file.toURI().toURL());
			}
		}
		catch (IOException e) {
			logger.log(Level.WARNING, "Exception occurred while opening file: " + file, e); //$NON-NLS-1$
		}
	}

	private boolean checkForBadFileName(String url) {
		boolean bad = false;
		if (url.startsWith(ClientConstants.DBSLASH)) {
			bad = true;
		}
		else if (url.contains(ClientConstants.AMP)) {
			bad = true;
		}
		else if (url.contains(ClientConstants.LSBRA)) {
			bad = true;
		}
		else if (url.contains(ClientConstants.RSBRA)) {
			bad = true;
		}
		else if (url.contains(ClientConstants.LRBRA)) {
			bad = true;
		}
		else if (url.contains(ClientConstants.RRBRA)) {
			bad = true;
		}
		else if (url.contains(ClientConstants.PLUS)) {
			bad = true;
		}
		else if (url.contains(ClientConstants.FTICK)) {
			bad = true;
		}
		else if (url.contains(ClientConstants.BTICK)) {
			bad = true;
		}

		return bad;
	}

    public void openURL(URL url) throws IOException {
        String href = url.toString();
        openURL(href);
    }

	public void openURL(String url) throws IOException {
		logger.info("Opening url " + url);

		String href = url;
		if (href.startsWith(ClientConstants.STRING_FILE)) {
			href = href.substring(5);
			while (href.startsWith(ClientConstants.FSLASH)) {
				href = href.substring(1);
			}
			href = ClientConstants.FILE_PROTOCOL + href;

		}

		if(isDisabledForTest()) {
			logger.info("Not showing document '" + href + "', system property for test is set");
			return;
		}

		final String localHref = href;
		if(SystemUtils.IS_OS_UNIX) {
			try {
				Runtime.getRuntime().exec("gnome-open " + href);
			} catch (IOException e) {
				// try kde-open if gnome-open did not work
				Runtime.getRuntime().exec("kde-open " + href);
			}
		} else {
			Runtime.getRuntime().exec(ArrayUtils.addAll(ClientConstants.CMD_C_START_ARRAY, localHref.replace("&", "^&")));
		}
	}

	public static boolean isDisabledForTest() {
		return "true".equalsIgnoreCase(System.getProperty(PROPERTY_DOCUMENT_STARTER_DISABLE));
	}
}
