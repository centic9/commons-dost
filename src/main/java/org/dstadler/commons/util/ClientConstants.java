package org.dstadler.commons.util;

/**
 * Just copied from general ClientConstants to not introduce a dependency here.
 *
 * @author dominik.stadler
 *
 */
public class ClientConstants {
    public static final String DOT = ".";
    public static final String COLON = ":";
    public static final String DBSLASH = "\\\\";
    public static final String FSLASH = "/";
    public static final String AMP = "&"; // CharacterConstants.AMP;
    public static final String WS = " "; // 1 whitespace
    public static final String PLUS = "+";
    public static final String FTICK = "\u00B4"; // Should be char "U+00B4", must not be "U+0092"(like in some codepages)
    public static final String BTICK = "\u0060";

    public static final String[] CMD_C_START_ARRAY = new String[] {"cmd", "/c", "start"};		// NOSONAR
    public static final String FILE_PROTOCOL = "file:///";
    public static final String STRING_FILE = "file:";

    /** * round brackets ** */
    public static final String LRBRA = "(";
    public static final String RRBRA = ")";

    public static final String RSBRA = "]";
    public static final String LSBRA = "[";
}
