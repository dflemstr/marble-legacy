package org.marble.settings;

import java.util.prefs.Preferences;

/**
 * An integer settings entry.
 */
class IntegerEntry extends AbstractEntry<Integer> {
    IntegerEntry(final Preferences prefs, final String node,
            final Integer defaultValue) {
        super(prefs, node, defaultValue);
    }

    @Override
    public Integer getValue() {
        return prefs.getInt(node, defaultValue);
    }

    @Override
    public void putValue(final Integer value) {
        prefs.putInt(node, value);
    }
}
