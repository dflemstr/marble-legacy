package org.marble.settings;

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
    protected final String node;
    protected final A defaultValue;

    /**
     * Constructs a new settings entry.
     * 
     * @param node
     *            The settings node to store this setting in. Can be a
     *            slash-separated path in the preferences registry.
     * @param defaultValue
     *            The default value for this settings entry, if a value hasn't
     *            already been saved.
     * @param settings
     *            TODO
     */
    protected AbstractEntry(final Preferences prefs, final String node,
            final A defaultValue) {
        this.prefs = prefs;
        this.node = node;
        this.defaultValue = defaultValue;
    }
}
