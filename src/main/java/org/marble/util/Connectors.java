package org.marble.util;

import static com.ardor3d.math.FastMath.cos;
import static com.ardor3d.math.FastMath.sin;

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

        final float sinPitch = (float) sin(pitch);
        final float cosPitch = (float) cos(pitch);
        final float sinRoll = (float) sin(roll);
        final float cosRoll = (float) cos(roll);
        final float sinYaw = (float) sin(yaw);
        final float cosYaw = (float) cos(yaw);

        transform.setElement(0, 0, cosPitch * cosYaw);
        transform.setElement(0, 1, -sinYaw * cosPitch);
        transform.setElement(0, 2, sinPitch);
        transform.setElement(1, 0, cosYaw * sinPitch * sinRoll + sinYaw
                * cosRoll);
        transform.setElement(1, 1, -sinYaw * sinPitch * sinRoll + cosYaw
                * cosRoll);
        transform.setElement(1, 2, -cosPitch * sinRoll);
        transform.setElement(2, 0, -cosYaw * sinPitch * cosRoll + sinYaw
                * sinRoll);
        transform.setElement(2, 1, sinYaw * sinPitch * cosRoll + cosYaw
                * sinRoll);
        transform.setElement(2, 2, cosPitch * cosRoll);

        return new Connector(transform);
    }

    private Connectors() {
    }
}
