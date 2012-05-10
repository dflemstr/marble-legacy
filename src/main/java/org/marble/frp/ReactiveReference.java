package org.marble.frp;

import org.marble.frp.mutable.AbstractMutableReactive;

public class ReactiveReference<A> extends AbstractMutableReactive<A> {
    private A value;

    public ReactiveReference(final A value) {
        this.value = value;
    }

    @Override
    public A getValue() {
        return value;
    }

    @Override
    protected void putValue(final A value) {
        this.value = value;
    }

}
