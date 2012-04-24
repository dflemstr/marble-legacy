package org.marble.util;

public enum Quality {
    Lowest("Lowest", 0), Lower("Lower", 1), Low("Low", 2), Medium("Medium", 3),
    High("High", 4), Higher("Higher", 5), Highest("Highest", 6);
    private final String humanReadable;
    private final int index;

    private Quality(final String humanReadable, final int index) {
        this.humanReadable = humanReadable;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public String getHumanReadable() {
        return humanReadable;
    }
}
