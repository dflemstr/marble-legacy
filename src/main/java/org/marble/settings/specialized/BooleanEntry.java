package org.marble.settings.specialized;

import java.util.prefs.Preferences;

import org.marble.settings.AbstractEntry;

/**
 * A boolean settings entry.
 */
public class BooleanEntry extends AbstractEntry<Boolean> {
    public BooleanEntry(final Preferences prefs, final String node,
            final Boolean defaultValue) {
        super(prefs, node, defaultValue);
    }

    @Override
    public Boolean getValue() {
        return prefs.getBoolean(node, defaultValue);
    }

    @Override
    public void putValue(final Boolean value) {
        prefs.putBoolean(node, value);
    }
}
