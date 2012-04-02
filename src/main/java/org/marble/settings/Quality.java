package org.marble.settings;

public enum Quality {
    Lowest("Lowest"), Lower("Lower"), Low("Low"), Medium("Medium"),
    High("High"), Higher("Higher"), Highest("Highest");
    private final String humanReadable;

    private Quality(final String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return humanReadable;
    }
}
