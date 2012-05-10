package org.marble.frp.mutable;

import org.marble.frp.Reactive;

public interface MutableReactive<A> extends Reactive<A> {
    public void setValue(A value);
}
