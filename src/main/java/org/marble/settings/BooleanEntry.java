package org.marble.settings;

import java.util.prefs.Preferences;

/**
 * A boolean settings entry.
 */
class BooleanEntry extends Entry<Boolean> {
    BooleanEntry(final Preferences prefs, final String node,
            final Boolean defaultValue) {
        super(prefs, node, defaultValue);
    }

    @Override
    public Boolean getValue() {
        return prefs.getBoolean(node, defaultValue);
    }

    @Override
    public void setValue(final Boolean value) {
        prefs.putBoolean(node, value);
    }

}
