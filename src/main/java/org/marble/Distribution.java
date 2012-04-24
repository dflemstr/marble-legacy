package org.marble;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public final class Distribution {
    private Distribution() {
    }

    private static final String programName = "Marble";
    private static final int versionMajor = 1;
    private static final int versionMinor = 0;
    private static final ImmutableList<String> versionTags = ImmutableList
            .of("snapshot");
    private static String copyright =
            "\u00a9 David Flemström & Fabian Bergmark 2012";

    public static String getProgramName() {
        return programName;
    }

    public static int getVersionMajor() {
        return versionMajor;
    }

    public static int getVersionMinor() {
        return versionMinor;
    }

    public static ImmutableList<String> getVersionTags() {
        return versionTags;
    }

    public static String getCopyright() {
        return copyright;
    }

    public static String getProgramDescription() {
        return String.format("%s %d.%d-%s, %s", getProgramName(),
                getVersionMajor(), getVersionMinor(),
                Joiner.on('-').join(getVersionTags()), getCopyright());
    }
}
