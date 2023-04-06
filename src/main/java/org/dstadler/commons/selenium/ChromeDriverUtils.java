package org.dstadler.commons.selenium;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.dstadler.commons.exec.ExecutionHelper;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.commons.util.SuppressForbidden;
import org.dstadler.commons.zip.ZipUtils;

/**
 * Utility for downloading the matching version of the
 * Selenium ChromeDriver for the locally installed version
 * of Chrome.
 *
 * When using Selenium, it uses the ChromeDriver to talk to
 * the locally installed Chrome browser.
 *
 * Usually you need to manually download the matching version
 * of the chrome-driver binary and put it in place.
 *
 * This class automates this by determining the local version
 * of the chrome-browser (by execution it with "--version") and
 * then looking for the matching version of chrome-driver on
 * <a href="https://sites.google.com/a/chromium.org/chromedriver/downloads/version-selection">this page</a>
 */
public class ChromeDriverUtils {
    private static final Logger log = LoggerFactory.make();

    public static final String PROPERTY_CHROME_DRIVER = "webdriver.chrome.driver";

    /**
     * Check which version of chrome is installed and based on its version
     * try to fetch the matching chromedriver and configure it via system
     * properties.
     *
     * @throws java.io.IOException If fetching data or storing data in files fails.
     */
    public static void configureMatchingChromeDriver() throws IOException {
        configureMatchingChromeDriver(getGoogleChromeVersion());
    }

    /**
     * For a given major version of chrome, try to fetch the matching chromedriver
     * and configure it via system properties.
     *
     * @param chromeVersion The version of chrome, e.g. "100" or "101.0.4951.54"
     * @throws java.io.IOException If fetching data or storing data in files fails.
     */
    public static void configureMatchingChromeDriver(String chromeVersion) throws IOException {
		checkState(StringUtils.isNotBlank(chromeVersion), "Need a version of chrome to configure, but had '" +
						chromeVersion + "'");

        // See https://sites.google.com/a/chromium.org/chromedriver/downloads/version-selection
        //
        // https://chromedriver.storage.googleapis.com/LATEST_RELEASE_91.0.4472.77
        // 91.0.4472.19
        // https://chromedriver.storage.googleapis.com/index.html?path=91.0.4472.19/
        // https://chromedriver.storage.googleapis.com/91.0.4472.19/chromedriver_linux64.zip
        // https://chromedriver.storage.googleapis.com/91.0.4472.19/chromedriver_win32.zip

        String versionUrl = "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_" + chromeVersion;
        final String driverVersion;
        try {
            driverVersion = IOUtils.toString(new URL(versionUrl), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Failed for " + versionUrl, e);
        }
        checkState(StringUtils.isNotBlank(driverVersion),
                "Did not find a chrome-driver-version for " + chromeVersion + " at " + versionUrl);

        File chromeDriverFile = new File("chromedriver-" + driverVersion +
                (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));

        // download the driver if not available locally yet
        if (!chromeDriverFile.exists()) {
            String downloadUrl = SystemUtils.IS_OS_WINDOWS ?
                    "https://chromedriver.storage.googleapis.com/" + driverVersion + "/chromedriver_win32.zip" :
                    "https://chromedriver.storage.googleapis.com/" + driverVersion + "/chromedriver_linux64.zip";

            log.info("Downloading matching chromedriver from " + downloadUrl +
                    " and extracting to " + chromeDriverFile);

            File fileZip = new File("/tmp/chromedriver.zip");
            FileUtils.copyURLToFile(new URL(downloadUrl), fileZip);

            // unzip the driver-files to the local directory
            ZipUtils.extractZip(fileZip, new File("."));

            // rename them to the proper version-name
            FileUtils.moveFile(new File("chromedriver" +
                    (SystemUtils.IS_OS_WINDOWS ? ".exe" : "")), chromeDriverFile);
        }

        // make sure the binary is executable
        if (!chromeDriverFile.canExecute()) {
            if (!chromeDriverFile.setExecutable(true)) {
                throw new IOException("Could not make binary " + chromeDriverFile + " executable.");
            }
        }

        log.info("Using chromedriver from " + chromeDriverFile.getAbsolutePath());
        System.setProperty(PROPERTY_CHROME_DRIVER, chromeDriverFile.getAbsolutePath());
    }

    /**
     * Call 'google-chrome-stable --version' and parse the output to
     * get the current available version of the Chrome browser.
     *
     * @return A string with the full version of the locally installed Chrome
     *          browser, e.g. '91.0.4472.77'
     * @throws java.io.IOException If executing chrome fails.
     */
    protected static String getGoogleChromeVersion() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Google Chrome 91.0.4472.77
		String version;
		if (SystemUtils.IS_OS_WINDOWS) {
			CommandLine cmdLine = new CommandLine("reg");
			cmdLine.addArgument("query");
			cmdLine.addArgument("\"HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon\"");
			cmdLine.addArgument("/v");
			cmdLine.addArgument("version");

			ExecutionHelper.getCommandResultIntoStream(cmdLine, new File("."), 0, 10_000, out);
			// cut out the leading text
			version = out.toString().replace("HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon", "").
					replace("version", "").
					replace("REG_SZ", "").
					trim();
		} else {
			CommandLine cmdLine = new CommandLine("google-chrome-stable");
			cmdLine.addArgument("--version");

            try {
                ExecutionHelper.getCommandResultIntoStream(cmdLine, new File("."), 0, 10_000, out);
                // cut out the leading text
                version = StringUtils.removeStart(out.toString(), "Google Chrome ").trim();
            } finally {
                out.close();
                log.info("Having result from calling '" + cmdLine + "': " + out.toString(StandardCharsets.UTF_8).trim());
            }
        }

        // cut off the trailing patch-level
        try {
            version = version.substring(0, version.lastIndexOf('.'));
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed for '" + out + "'", e);
        }

        log.info("Found Google Chrome version '" + version + "' from running with --version: '" + out.toString().trim() + "'");

        return version;
    }

    /**
     * Clean up any system property or otherwise held information.
     */
    @SuppressForbidden(reason = "This is provided on purpose here")
    protected static void cleanUp() {
        System.clearProperty(PROPERTY_CHROME_DRIVER);
    }

    private static void checkState(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }
}
