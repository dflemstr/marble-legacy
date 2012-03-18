package org.marble.util;

import javax.vecmath.Matrix4f;

import org.marble.entity.Connector;

public final class Connectors {
    public static Connector
            offsetBy(final float x, final float y, final float z,
                    final float yaw, final float pitch, final float roll) {
        final Matrix4f transform = new Matrix4f();
        transform.setIdentity();
        transform.setElement(0, 3, x);
        transform.setElement(1, 3, y);
        transform.setElement(2, 3, z);

        // TODO make this efficient...
        /*
         * final Matrix4f rotation = new Matrix4f(); rotation.rotZ(yaw);
         * transform.mul(rotation); rotation.rotX(pitch);
         * transform.mul(rotation); rotation.rotY(roll);
         * transform.mul(rotation);
         */

        return new Connector(transform);
    }

    private Connectors() {
    }
}
