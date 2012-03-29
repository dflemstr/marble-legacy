package org.marble.settings;

import java.util.prefs.Preferences;

/**
 * An entry in the settings registry.
 * 
 * @param <A>
 *            The type of the setting being stored in this entry.
 */
public abstract class Entry<A> {
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
    protected Entry(final Preferences prefs, final String node,
            final A defaultValue) {
        this.prefs = prefs;
        this.node = node;
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the value of this settings entry.
     * 
     * @return The value of this settings entry, or the default value if no
     *         value is present.
     */
    public abstract A getValue();

    /**
     * Changes the value of this entry.
     * 
     * @param value
     *            The value to change to.
     */
    public abstract void setValue(A value);
}