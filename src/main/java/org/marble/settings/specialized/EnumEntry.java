package org.marble.settings.specialized;

import java.util.prefs.Preferences;

import com.google.common.collect.ImmutableMap;

import org.marble.settings.AbstractEntry;

/**
 * A renderer settings entry.
 */
public class EnumEntry<E extends Enum<E>> extends AbstractEntry<E> {
    private final ImmutableMap<String, E> alternatives;

    public EnumEntry(final Preferences prefs, final String node,
            final E defaultValue, final Class<E> enumClass) {
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
        final String name = prefs.get(key, defaultValue.name());
        if (alternatives.containsKey(name))
            return alternatives.get(name);
        else
            throw new IllegalStateException("Invalid value for setting "
                    + prefs + ": " + name);
    }

    @Override
    public void putValue(final E value) {
        prefs.put(key, value.name());
    }

}
