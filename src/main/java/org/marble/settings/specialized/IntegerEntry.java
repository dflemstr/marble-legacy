package org.marble.settings.specialized;

import java.util.prefs.Preferences;

import org.marble.settings.AbstractEntry;

/**
 * An integer settings entry.
 */
public class IntegerEntry extends AbstractEntry<Integer> {
    public IntegerEntry(final Preferences prefs, final String node,
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
