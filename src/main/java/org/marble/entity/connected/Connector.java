package org.marble.entity.connected;

import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;

import com.google.common.base.Objects;

public class Connector {
    private static final Matrix4f rotate;
    static {
        rotate = new Matrix4f();
        rotate.angleRotation(new Vector3f(0, 0, 180));
    }

    private final Matrix4f offset;

    public Connector(final Matrix4f offset) {
        this.offset = offset;
    }

    public Vector3f getTranslation() {
        return offset.toTranslationVector();
    }

    public void rotate(final Vector3f vector) {
        rotate.mult(vector, vector);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("offset", offset).toString();
    }

    public void transform(final Vector3f vector) {
        rotate.mult(vector, vector);
        offset.mult(vector, vector);
    }

    public void transformInto(final Connector that, final Matrix4f transform) {
        that.offset.invert(transform);
        rotate.mult(transform, transform);
        offset.mult(transform, transform);
    }
}
