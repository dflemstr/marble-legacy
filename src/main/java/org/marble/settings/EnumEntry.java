package org.marble.settings;

import java.util.prefs.Preferences;

import com.google.common.collect.ImmutableMap;

/**
 * A renderer settings entry.
 */
class EnumEntry<E extends Enum<E>> extends Entry<E> {
    private final ImmutableMap<String, E> alternatives;

    EnumEntry(final Preferences prefs, final String node, final E defaultValue,
            final Class<E> enumClass) {
        super(prefs, node, defaultValue);

        final ImmutableMap.Builder<String, E> alternativesBuilder =
                ImmutableMap.builder();
        for (final E alternative : enumClass.getEnumConstants()) {
            alternativesBuilder.put(alternative.name(), alternative);
        }
        alternatives = alternativesBuilder.build();
    }

    @Override
    public E getValue() {
        final String name = prefs.get(node, defaultValue.name());
        if (alternatives.containsKey(name))
            return alternatives.get(name);
        else
            throw new IllegalStateException("Invalid value for setting " + node
                    + ": " + name);
    }

    @Override
    public void setValue(final E value) {
        prefs.put(node, value.name());
    }

}