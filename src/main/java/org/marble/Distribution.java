package org.marble;

/**
 * Information about this distribution of the program.
 */
public final class Distribution {
    private static String copyright =
            "\u00a9 David Flemström & Fabian Bergmark 2012";

    private static final String programName = "Marble";
    private static final String version = "1.0-SNAPSHOT";

    private Distribution() {
    }

    /**
     * The application's copyright information.
     */
    public static String getCopyright() {
        return copyright;
    }

    /**
     * Formats a description of the program including all of the data in the
     * distribution information.
     */
    public static String getProgramDescription() {
        return String.format("%s %s, %s", getProgramName(), getVersion(),
                getCopyright());
    }

    /**
     * The short name of this application.
     */
    public static String getProgramName() {
        return programName;
    }

    /**
     * The application's version, in Maven-compatible format.
     */
    public static String getVersion() {
        return version;
    }
}
