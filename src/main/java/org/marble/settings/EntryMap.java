package org.marble.settings;

import org.marble.frp.Reactive;

public interface EntryMap<A, B> {
    public Reactive<B> getEntry(A a);
}
