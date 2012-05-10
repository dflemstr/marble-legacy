package org.marble.settings;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import org.marble.frp.mutable.AbstractMutableReactive;

/**
 * An entry in the settings registry.
 * 
 * @param <A>
 *            The type of the setting being stored in this entry.
 */
public abstract class AbstractEntry<A> extends AbstractMutableReactive<A> {
    protected final Preferences prefs;
    protected final String key;
    protected final A defaultValue;

    /**
     * Constructs a new settings entry.
     */
    protected AbstractEntry(final Preferences prefs, final String node,
            final A defaultValue) {
        final int lastSeparator = node.lastIndexOf('/');
        if (lastSeparator < 0) {
            key = node;
            this.prefs = prefs;
        } else {
            key = node.substring(lastSeparator + 1);
            this.prefs = prefs.node(node.substring(0, lastSeparator));
        }
        this.defaultValue = defaultValue;

        prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(final PreferenceChangeEvent evt) {
                if (evt.getNode().equals(prefs)) {
                    setValue(getValue());
                }
            }
        });
    }
}
