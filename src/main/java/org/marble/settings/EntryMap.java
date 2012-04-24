package org.marble.settings;

public interface EntryMap<A, B> {
    public Entry<B> getEntry(A a);
}
