package org.marble.entity;

import javax.vecmath.Matrix4f;

public class Connector {
    private final Matrix4f offset;
    private static final Matrix4f mirrorX = new Matrix4f();

    static {
        mirrorX.setIdentity();
        mirrorX.setElement(0, 0, -1);
    }

    public Connector(final Matrix4f offset) {
        this.offset = offset;
    }

    public void transformInto(final Connector that, final Matrix4f out) {
        out.invert(that.offset);
        out.mul(mirrorX);
        out.mul(offset, out);
    }
}
