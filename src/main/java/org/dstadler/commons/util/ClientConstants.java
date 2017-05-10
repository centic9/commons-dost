package org.dstadler.commons.util;

/**
 * Just copied from general ClientConstants to not introduce a dependency here.
 *
 * @author dominik.stadler
 *
 */
public class ClientConstants {

    /** * BROWSERS ** */
    public static final String BROWSER_NETSCAPE = "netscape";
    public static final String BROWSER_MOZILLA = "mozilla";
    public static final String BROWSER_FIREFOX = "firefox";
    public static final String BROWSER_GNOME_OPEN = "gnome-open";
    public static final String BROWSER_KDE_OPEN = "kde-open";

    // ordered list of choices that are checked when trying to open a file
    public static final String[] BROWSER_CHOICES = { BROWSER_GNOME_OPEN, BROWSER_KDE_OPEN, BROWSER_FIREFOX, BROWSER_MOZILLA, BROWSER_NETSCAPE };

    public static final String DOT = ".";
    public static final String COLON = ":";
    public static final String DBSLASH = "\\\\";
    public static final String FSLASH = "/";
    public static final String AMP = "&"; // CharacterConstants.AMP;
    public static final String DWS = "  "; // double whitespace
    public static final String WS = " "; // 1 whitespace
    public static final String PLUS = "+";
    public static final String FTICK = "\u00B4"; // Should be char "U+00B4", must not be "U+0092"(like in some codepages)
    public static final String BTICK = "\u0060";

    public static final String FORMAT_STRING_PERCENT_20 = "%20";
    public static final String PLATFORM_WIN32 = "win32";
    public static final String[] CMD_C_START_ARRAY = new String[] {"cmd", "/c", "start"};		// NOSONAR
    public static final String PLATFORM_CARBON = "carbon";
    public static final String USR_BIN_OPEN = "/usr/bin/open ";
    public static final String FILE_PROTOCOL = "file:///";
    public static final String STRING_FILE = "file:";

    /** * round brackets ** */
    public static final String WS_LRBRA = " (";
    public static final String LRBRA = "(";
    public static final String RRBRA = ")";
    public static final String RRBRA_WS = ") ";

    /** * angle brackets ** */
    public static final String RABRA_WS = "> ";
    public static final String WS_LABRA = " <";
    public static final String RABRA = ">";
    public static final String LABRA = "<";
    /** * curly brackets ** */
    public static final String RCBRA_WS = "} ";
    public static final String WS_LCBRA = " {";
    public static final String RCBRA = "}";
    public static final String LCBRA = "{";
    /** * square brackets ** */
    public static final String RSBRA_WS = "] ";
    public static final String WS_LSBRA = " [";
    public static final String RSBRA = "]";
    public static final String LSBRA = "[";
}
