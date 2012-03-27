package org.marble.settings;

import java.util.prefs.Preferences;

/**
 * A string settings entry.
 */
class StringEntry extends Entry<String> {
    StringEntry(final Preferences prefs, final String node,
            final String defaultValue) {
        super(prefs, node, defaultValue);
    }

    @Override
    public String getValue() {
        return prefs.get(node, defaultValue);
    }

    @Override
    public void setValue(final String value) {
        prefs.put(node, value);
    }
}