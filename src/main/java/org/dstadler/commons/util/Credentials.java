package org.dstadler.commons.util;

import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class Credentials {
    private final static Logger log = LoggerFactory.make();

    private static Properties properties;

    public static Properties loadCredentials() {
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

    public static String getCredentialOrNull(String key) {
        if (properties == null) {
            loadCredentials();
        }

        String result = properties.getProperty(key, null);
        if (result == null &&
                // only log here if we did not log a problem with overall loading of the credentials before
                !properties.isEmpty()) {
            log.severe("No credential '" + key + "' found in 'credentials.properties'.");
        }
        return result;
    }

    public static String getCredentialOrFail(String key) {
    	String result = getCredentialOrNull(key);
    	if(result == null) {
    		throw new IllegalStateException("Could not read credentials for key '" + key + "'");
    	}

    	return result;
    }
}
