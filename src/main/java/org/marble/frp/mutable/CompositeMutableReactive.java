package org.marble.frp.mutable;

import org.apache.commons.lang3.tuple.Pair;

import org.marble.frp.CompositeReactive;

public class CompositeMutableReactive<A, B> extends CompositeReactive<A, B>
        implements MutableReactive<Pair<A, B>> {
    private final MutableReactive<A> entryA;
    private final MutableReactive<B> entryB;

    public CompositeMutableReactive(final MutableReactive<A> entryA,
            final MutableReactive<B> entryB) {
        super(entryA, entryB);
        this.entryA = entryA;
        this.entryB = entryB;
    }

    @Override
    public void setValue(final Pair<A, B> value) {
        entryA.setValue(value.getLeft());
        entryB.setValue(value.getRight());
    }
}
