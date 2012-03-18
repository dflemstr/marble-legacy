package org.marble.entity;

import javax.vecmath.Matrix4f;

public class Connector {
    private final Matrix4f offset;

    public Connector(final Matrix4f offset) {
        this.offset = offset;
    }

    public void transformInto(final Connector that, final Matrix4f out) {
        out.set(that.offset);
        out.invert();
        out.mul(offset);
    }
}
