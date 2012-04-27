package org.marble.frp;

import java.util.Set;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.tuple.Pair;

public class CompositeReactive<A, B> implements Reactive<Pair<A, B>> {
    private final class EntryListenerB implements ReactiveListener<B> {
        @Override
        public void valueChanged(final B value) {
            emitChange(entryA.getValue(), value);
        }
    }

    private final class EntryListenerA implements ReactiveListener<A> {
        @Override
        public void valueChanged(final A value) {
            emitChange(value, entryB.getValue());
        }
    }

    private final Reactive<A> entryA;
    private final Reactive<B> entryB;
    private final Set<ReactiveListener<Pair<A, B>>> listeners = Sets
            .newHashSet();

    public CompositeReactive(final Reactive<A> entryA, final Reactive<B> entryB) {
        this.entryA = entryA;
        this.entryB = entryB;
        entryA.addReactiveListener(new EntryListenerA());
        entryB.addReactiveListener(new EntryListenerB());
    }

    private void emitChange(final A a, final B b) {
        final Pair<A, B> pair = Pair.of(a, b);
        for (final ReactiveListener<Pair<A, B>> listener : listeners) {
            listener.valueChanged(pair);
        }
    }

    @Override
    public Pair<A, B> getValue() {
        return Pair.of(entryA.getValue(), entryB.getValue());
    }

    @Override
    public void
            addReactiveListener(final ReactiveListener<Pair<A, B>> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeReactiveListener(
            final ReactiveListener<Pair<A, B>> listener) {
        listeners.remove(listeners);
    }
}
