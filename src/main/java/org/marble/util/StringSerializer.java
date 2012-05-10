package org.marble.util;

public interface StringSerializer<A> {
    public A fromString(String string);

    public String toString(A a);
}
