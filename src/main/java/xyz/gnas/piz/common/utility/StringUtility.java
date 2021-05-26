package xyz.gnas.piz.common.utility;

/**
 * Provides utility methods for string
 */
public class StringUtility {
    /**
     * Gets quoted string. ("some string")
     *
     * @param s the string
     * @return the quoted string
     */
    public static String getQuotedString(String s) {
        return "\"" + s + "\"";
    }
}
