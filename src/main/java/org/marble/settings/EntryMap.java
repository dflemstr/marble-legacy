package org.marble.settings;

import org.marble.frp.mutable.MutableReactive;

public interface EntryMap<A, B> {
    public MutableReactive<B> getEntry(A a);
}
