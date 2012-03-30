package org.marble.level;

import com.google.common.collect.ImmutableList;

public class MetaLevelPack {
    private final String name;
    private final String version;
    private final String description;
    private final String author;
    private final ImmutableList<MetaLevel> levels;

    public MetaLevelPack(final String name, final String version,
            final String description, final String author,
            final ImmutableList<MetaLevel> levels) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.author = author;

        this.levels = levels;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public ImmutableList<MetaLevel> getLevels() {
        return levels;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }
}
