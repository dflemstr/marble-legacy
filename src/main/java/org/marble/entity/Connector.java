package org.marble.entity;

import javax.vecmath.Matrix4f;

import com.google.common.base.Objects;

public class Connector {
    private final Matrix4f offset;
    private static final Matrix4f rotate;

    static {
        rotate = new Matrix4f();
        rotate.rotZ((float) Math.PI);
    }

    public Connector(final Matrix4f offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("offset", offset).toString();
    }

    public void transformInto(final Connector that, final Matrix4f out) {
        out.invert(that.offset);
        out.mul(rotate, out);
        out.mul(offset, out);
    }
}
