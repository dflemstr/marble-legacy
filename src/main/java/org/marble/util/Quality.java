package org.marble.util;

public enum Quality {
    High("High", 4), Higher("Higher", 5), Highest("Highest", 6), Low("Low", 2),
    Lower("Lower", 1), Lowest("Lowest", 0), Medium("Medium", 3);
    private final String humanReadable;
    private final int index;

    private Quality(final String humanReadable, final int index) {
        this.humanReadable = humanReadable;
        this.index = index;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public int getIndex() {
        return index;
    }
}
