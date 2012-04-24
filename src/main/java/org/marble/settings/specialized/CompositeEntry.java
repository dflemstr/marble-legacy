package org.marble.settings.specialized;

import java.util.Set;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.tuple.Pair;

import org.marble.settings.Entry;
import org.marble.settings.EntryListener;

public class CompositeEntry<A, B> implements Entry<Pair<A, B>> {
    private final class EntryListenerB implements EntryListener<B> {
        @Override
        public void entryChanged(final B value) {
            emitChange(entryA.getValue(), value);
        }
    }

    private final class EntryListenerA implements EntryListener<A> {

        @Override
        public void entryChanged(final A value) {
            emitChange(value, entryB.getValue());
        }
    }

    private final Entry<A> entryA;
    private final Entry<B> entryB;
    private final Set<EntryListener<Pair<A, B>>> listeners = Sets.newHashSet();

    public CompositeEntry(final Entry<A> entryA, final Entry<B> entryB) {
        this.entryA = entryA;
        this.entryB = entryB;
        entryA.addEntryListener(new EntryListenerA());
        entryB.addEntryListener(new EntryListenerB());
    }

    private void emitChange(final A a, final B b) {
        final Pair<A, B> pair = Pair.of(a, b);
        for (final EntryListener<Pair<A, B>> listener : listeners) {
            listener.entryChanged(pair);
        }
    }

    @Override
    public Pair<A, B> getValue() {
        return Pair.of(entryA.getValue(), entryB.getValue());
    }

    @Override
    public void setValue(final Pair<A, B> value) {
        entryA.setValue(value.getLeft());
        entryB.setValue(value.getRight());
    }

    @Override
    public void addEntryListener(final EntryListener<Pair<A, B>> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEntryListener(final EntryListener<Pair<A, B>> listener) {
        listeners.remove(listeners);
    }
}
