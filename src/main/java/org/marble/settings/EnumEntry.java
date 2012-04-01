package org.marble.settings;

import java.util.prefs.Preferences;

/**
 * A renderer settings entry.
 */
class EnumEntry<E extends Enum<E>> extends Entry<E> {
    private final E[] alternatives;

    EnumEntry(final Preferences prefs, final String node,
            final E defaultValue,
            final Class<E> enumClass) {
        super(prefs, node, defaultValue);
        this.alternatives = enumClass.getEnumConstants();
    }

    @Override
    public E getValue() {
        final String name = prefs.get(node, defaultValue.name());
        for (final E alternative : alternatives) {
            if (alternative.name().equals(name))
                return alternative;
        }
        throw new IllegalStateException("Invalid value for setting " + node
                + ": " + name);
    }

    @Override
    public void setValue(final E value) {
        prefs.put(node, value.name());
    }

}