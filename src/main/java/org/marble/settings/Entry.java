package org.marble.settings;

public interface Entry<A> {
    public A getValue();

    public void setValue(A value);

    public void addEntryListener(EntryListener<A> listener);

    public void removeEntryListener(EntryListener<A> listener);
}
