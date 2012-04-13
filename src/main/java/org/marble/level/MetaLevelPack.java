package org.marble.level;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class MetaLevelPack {
    private final String name;
    private final Optional<String> version;
    private final Optional<String> description;
    private final Optional<String> author;
    private final ImmutableList<MetaLevel> levels;

    public MetaLevelPack(final String name, final Optional<String> version,
            final Optional<String> description, final Optional<String> author,
            final ImmutableList<MetaLevel> levels) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.author = author;

        this.levels = levels;
    }

    public Optional<String> getAuthor() {
        return author;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public ImmutableList<MetaLevel> getLevels() {
        return levels;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getVersion() {
        return version;
    }
}
