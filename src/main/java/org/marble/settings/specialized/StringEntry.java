package org.marble.settings.specialized;

import java.util.prefs.Preferences;

import org.marble.settings.AbstractEntry;

/**
 * A string settings entry.
 */
public class StringEntry extends AbstractEntry<String> {
    public StringEntry(final Preferences prefs, final String node,
            final String defaultValue) {
        super(prefs, node, defaultValue);
    }

    @Override
    public String getValue() {
        return prefs.get(key, defaultValue);
    }

    @Override
    public void putValue(final String value) {
        prefs.put(key, value);
    }
}
