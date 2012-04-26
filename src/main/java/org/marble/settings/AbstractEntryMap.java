package org.marble.settings;

import java.util.Map;
import java.util.prefs.Preferences;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import org.marble.frp.Reactive;
import org.marble.settings.specialized.SerializerEntry;
import org.marble.util.StringSerializer;

public class AbstractEntryMap<A, B> implements EntryMap<A, B> {
    protected final Preferences node;
    protected final B defaultValue;
    protected final Function<A, String> keyTranslation;
    protected final StringSerializer<B> valueSerializer;
    private final Map<A, Reactive<B>> loadedEntries = Maps.newHashMap();

    public AbstractEntryMap(final Preferences prefs, final String baseNode,
            final B defaultValue, final Function<A, String> keyTranslation,
            final StringSerializer<B> valueSerializer) {
        this.node = prefs.node(baseNode);
        this.defaultValue = defaultValue;
        this.keyTranslation = keyTranslation;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public Reactive<B> getEntry(final A a) {
        if (loadedEntries.containsKey(a))
            return loadedEntries.get(a);
        final Reactive<B> entry =
                new SerializerEntry<B>(node, keyTranslation.apply(a),
                        defaultValue, valueSerializer);
        loadedEntries.put(a, entry);
        return entry;
    }
}
