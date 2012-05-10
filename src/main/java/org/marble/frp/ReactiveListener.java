package org.marble.frp;

public interface ReactiveListener<A> {
    public void valueChanged(A value);
}
