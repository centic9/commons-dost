package org.dstadler.commons.util;

import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Helper class to fetch credentials from a file "credentials.properties".
 *
 * The file can be located in the current directory or in the parent directory (..)
 * or in a directory "resources/".
 */
public class Credentials {
    private final static Logger log = LoggerFactory.make();

    private static Properties properties;

	/**
	 * Loads properties from a file "credentials.properties".
	 *
	 * Ensures data is replaced in a thread-safe way
	 *
	 * Failures to load the file are caught and reported
	 * as severe logs, no IOException is thrown.
	 *
	 * An empty set of properties is returned if no credentials file can be found
	 *
	 * @return The properties loaded from the file or empty when no credentials are found..
	 */
    public synchronized static Properties loadCredentials() {
		// are properties already initialized?
		if (properties != null) {
			return properties;
		}

        properties = new Properties();

        File file = new File("credentials.properties");
        if (!file.exists()) {
            file = new File("resources/credentials.properties");
        }
        if (!file.exists()) {
            file = new File("../credentials.properties");
        }

        loadProperties(file);

        return properties;
    }

	/**
	 * Load properties from the given file.
	 *
	 * Failures to load the file are caught and reported
	 * as severe logs, no IOException is thrown.
	 *
	 * @param file The text-file to read credentials.
	 */
    protected static void loadProperties(File file) {
        try (FileInputStream fileStream = new FileInputStream(file)) {
            properties.load(fileStream);
            if(properties.isEmpty()) {
                log.warning("Did not load any properties from file " + file.getAbsolutePath());
            }
        } catch (FileNotFoundException e) {
            log.severe("Credentials file 'credentials.properties' not found: " + e);
        } catch (IOException e) {
            log.severe("Unable to access credentials file 'credentials.properties': " + e);
        }
    }

	/**
	 * Get the given credential or return null.
	 *
	 * @param key The credential to read.
	 * @return The value found in the credentials or null if not found.
	 */
    public static String getCredentialOrNull(String key) {
		loadCredentials();

        String result = properties.getProperty(key, null);
        if (result == null &&
                // only log here if we did not log a problem with overall loading of the credentials before
                !properties.isEmpty()) {
            log.severe("No credential '" + key + "' found in 'credentials.properties'.");
        }
        return result;
    }

	/**
	 * Get the given credential or throw an exception.
	 *
	 * @param key The credential to read.
	 * @return The value found in the credentials.
	 * @throws java.lang.IllegalStateException If no value is set for the given credential
	 */
    public static String getCredentialOrFail(String key) {
    	String result = getCredentialOrNull(key);
    	if(result == null) {
    		throw new IllegalStateException("Could not read credentials for key '" + key + "'");
    	}

    	return result;
    }
}
