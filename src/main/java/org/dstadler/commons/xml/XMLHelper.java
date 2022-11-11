package org.dstadler.commons.xml;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;

import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Helper methods for working with javax.xml classes.
 *
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">OWASP XXE</a>
 */
@SuppressWarnings("HttpUrlsUsage")
public final class XMLHelper {
    static final String FEATURE_LOAD_DTD_GRAMMAR = "http://apache.org/xml/features/nonvalidating/load-dtd-grammar";
    static final String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    static final String FEATURE_DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
    static final String FEATURE_EXTERNAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    static final String PROPERTY_ENTITY_EXPANSION_LIMIT = "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit";
    static final String PROPERTY_SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    static final String METHOD_ENTITY_EXPANSION_XERCES = "setEntityExpansionLimit";

    static final String[] SECURITY_MANAGERS = {
            //"com.sun.org.apache.xerces.internal.util.SecurityManager",
            "org.apache.xerces.util.SecurityManager"
    };

	private final static Logger LOG = LoggerFactory.make();
    private static long lastLog;

    @FunctionalInterface
    private interface SecurityFeature {
        void accept(String name, boolean value) throws ParserConfigurationException, SAXException, TransformerException;
    }

    @FunctionalInterface
    private interface SecurityProperty {
        void accept(String name, Object value) throws SAXException;
    }

    private XMLHelper() {
    }

    public static SAXParserFactory getSAXParserFactory() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		trySet(factory::setFeature, FEATURE_SECURE_PROCESSING, true);
		trySet(factory::setFeature, FEATURE_LOAD_DTD_GRAMMAR, false);
		trySet(factory::setFeature, FEATURE_LOAD_EXTERNAL_DTD, false);
		trySet(factory::setFeature, FEATURE_EXTERNAL_ENTITIES, false);
		trySet(factory::setFeature, FEATURE_DISALLOW_DOCTYPE_DECL, true);
		return factory;
    }

    /**
     * Creates a new SAX XMLReader, with sensible defaults
     */
    public static XMLReader newXMLReader() throws IOException {
		XMLReader xmlReader;
		try {
			xmlReader = getSAXParserFactory().newSAXParser().getXMLReader();
		} catch (SAXException | ParserConfigurationException e) {
			throw new IOException(e);
		}
		xmlReader.setEntityResolver(XMLHelper::ignoreEntity);
        trySet(xmlReader::setFeature, FEATURE_SECURE_PROCESSING, true);
        trySet(xmlReader::setFeature, FEATURE_EXTERNAL_ENTITIES, false);
        Object manager = getXercesSecurityManager();
        if (manager == null || !trySet(xmlReader::setProperty, PROPERTY_SECURITY_MANAGER, manager)) {
            // separate old version of Xerces not found => use the builtin way of setting the property
            trySet(xmlReader::setProperty, PROPERTY_ENTITY_EXPANSION_LIMIT, 1);
        }
        return xmlReader;
    }

    private static Object getXercesSecurityManager() {
        // Try built-in JVM one first, standalone if not
        for (String securityManagerClassName : SECURITY_MANAGERS) {
            try {
                Object mgr = Class.forName(securityManagerClassName).getConstructor().newInstance();
                Method setLimit = mgr.getClass().getMethod(METHOD_ENTITY_EXPANSION_XERCES, Integer.TYPE);
                setLimit.invoke(mgr, 1);
                // Stop once one can be setup without error
                return mgr;
            } catch (ClassNotFoundException ignored) {
                // continue without log, this is expected in some setups
            } catch (Throwable e) {     // NOSONAR - also catch things like NoClassDefError here
                logThrowable(e, "SAX Feature unsupported", securityManagerClassName);
            }
        }

        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    private static boolean trySet(SecurityFeature feature, String name, boolean value) {
        try {
            feature.accept(name, value);
            return true;
        } catch (Exception e) {
            logThrowable(e, "SAX Feature unsupported", name);
        } catch (Error ame) {
            logThrowable(ame, "Cannot set SAX feature because outdated XML parser in classpath", name);
        }
        return false;
    }

    private static boolean trySet(SecurityProperty property, String name, Object value) {
        try {
            property.accept(name, value);
            return true;
        } catch (Exception e) {
            logThrowable(e, "SAX Feature unsupported", name);
        } catch (Error ame) {
            // ignore all top error object - GraalVM in native mode is not coping with java.xml error message resources
            logThrowable(ame, "Cannot set SAX feature because outdated XML parser in classpath", name);
        }
        return false;
    }

    private static void logThrowable(Throwable t, String message, String name) {
        if (System.currentTimeMillis() > lastLog + TimeUnit.MINUTES.toMillis(5)) {
            LOG.log(Level.WARNING, message + " [log suppressed for 5 minutes] " + name, t);
            lastLog = System.currentTimeMillis();
        }
    }

    private static InputSource ignoreEntity(String publicId, String systemId) {
        return new InputSource(new StringReader(""));
    }
}
