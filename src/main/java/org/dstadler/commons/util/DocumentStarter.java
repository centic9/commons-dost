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
            Runtime.getRuntime().exec("gnome-open" + ClientConstants.DWS + href);
		} else
		/*String platform = SWT.getPlatform();
		if(ClientConstants.PLATFORM_WIN32.equals(platform))*/ {
/*			if (checkForBadFileName(url)) {
				// is share or badname that can not be launched by start command
				Program.launch(localHref);
			}
			else*/ {
				// is local
					Runtime.getRuntime().exec(ArrayUtils.addAll(ClientConstants.CMD_C_START_ARRAY, localHref.replace("&", "^&")));
				/*}
				catch (Exception e) {
					//fallback
					Program.launch(localHref);
				}*/
			}
		}
		/*else if(ClientConstants.PLATFORM_CARBON.equals(platform)) {
			try {
				Runtime.getRuntime().exec(ClientConstants.USR_BIN_OPEN + localHref);
			}
			catch(IOException e) {
				logger.log(Level.WARNING, "Exception occurred while opening URL: " + localHref, e); //$NON-NLS-1$
			}
		}
		else  {
			Thread launcher = new Thread() {
				@Override
				public void run() {
					String encodedLocalHref = urlEncodeForSpaces(localHref.toCharArray());
					try {
						if(webBrowserOpened) {
							Runtime.getRuntime().exec(webBrowser + ClientConstants.REMOTE_OPEN_URL + encodedLocalHref + ClientConstants.RRBRA);
							return;
						}
					}
					catch (IOException e) {
		    			logger.log(Level.WARNING, "Exception occurred while opening URL: " + encodedLocalHref, e); //$NON-NLS-1$
					}

					webBrowserOpened = true;
					try {
						Process p = openWebBrowser(encodedLocalHref);
						if(p != null) {
							try {
								int ret = p.waitFor();
								if(ret != 0) {
									String msg = "Non-succesful response from external application, this indicates that there is no platform support for opening report files of this type."; //$NON-NLS-1$
			    	    			logger.log(Level.WARNING, msg);
									throw new IllegalArgumentException();
								}
							} catch (InterruptedException e) {
		    	    			logger.log(Level.WARNING, "Exception occurred while waiting for process to start, URL: " + encodedLocalHref, e); //$NON-NLS-1$
							}
						}
					}
					catch(IOException _ex) {
						String msg = "Exception occurred while opening URL: " + encodedLocalHref; //$NON-NLS-1$
		    			logger.log(Level.WARNING, msg, _ex);
						throw new IllegalArgumentException(msg);
					}
				}*/

			/*};
			launcher.start();
		}*/
	}

	public static boolean isDisabledForTest() {
		return "true".equalsIgnoreCase(System.getProperty(PROPERTY_DOCUMENT_STARTER_DISABLE));
	}

	/*private String urlEncodeForSpaces(char input[]) {
	    StringBuffer retu = new StringBuffer(input.length);
	    for (char element : input) {
			if(element == ' ') {
				retu.append(ClientConstants.FORMAT_STRING_PERCENT_20);
			}
			else {
				retu.append(element);
			}
		}

	    return retu.toString();
	}*/

	/*private Process openWebBrowser(String href) throws IOException {
		String webBrowser;
		Process p = null;
	    //if (webBrowser == null) {
	    	for(String browser : ClientConstants.BROWSER_CHOICES) {
				try {
					logger.info("Trying to run command '" + browser + "' to open url: " + href); //$NON-NLS-1$ //$NON-NLS-2$
	                webBrowser = browser;
	                p = Runtime.getRuntime().exec(webBrowser + ClientConstants.DWS + href);

	                // if we got here without error, we have found a useable application
	                break;
	            }
	            catch(IOException _ex) {
	                // ignore as we have more items to check
	            	//webBrowser = ClientConstants.BROWSER_MOZILLA;
	            }
	    	}
		//}

	    if (p == null) {
	    	webBrowser = ClientConstants.BROWSER_FIREFOX;
	        p = Runtime.getRuntime().exec(webBrowser + ClientConstants.WS + href);
	    }
	    return p;
	}*/
}
