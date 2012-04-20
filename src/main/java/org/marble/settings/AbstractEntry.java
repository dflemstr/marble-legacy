package org.marble.settings;

import java.util.Set;
import java.util.prefs.Preferences;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * An entry in the settings registry.
 * 
 * @param <A>
 *            The type of the setting being stored in this entry.
 */
public abstract class AbstractEntry<A> implements Entry<A> {
    protected final Preferences prefs;
    protected final String node;
    protected final A defaultValue;
    protected final Set<EntryListener<A>> listeners = Sets.newIdentityHashSet();

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

    /**
     * Returns the value of this settings entry.
     * 
     * @return The value of this settings entry, or the default value if no
     *         value is present.
     */
    @Override
    public abstract A getValue();

    /**
     * Changes the value of this entry.
     * 
     * @param value
     *            The value to change to.
     */
    @Override
    public void setValue(final A value) {
        final A oldValue = getValue();
        if (!Objects.equal(oldValue, value)) {
            for (final EntryListener<A> listener : listeners) {
                listener.entryChanged(value);
            }
        }
        putValue(value);
    }

    protected abstract void putValue(final A value);

    @Override
    public void addEntryListener(final EntryListener<A> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEntryListener(final EntryListener<A> listener) {
        listeners.remove(listener);
    }
}
