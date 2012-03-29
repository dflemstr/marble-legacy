package org.marble.entity;

import javax.vecmath.Matrix4d;

import com.google.common.base.Objects;

public class Connector {
    private final Matrix4d offset;
    private static final Matrix4d rotate;

    static {
        rotate = new Matrix4d();
        rotate.rotZ(Math.PI);
    }

    public Connector(final Matrix4d offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("offset", offset).toString();
    }

    public void transformInto(final Connector that, final Matrix4d transform) {
        transform.invert(that.offset);
        transform.mul(rotate, transform);
        transform.mul(offset, transform);
    }
}
