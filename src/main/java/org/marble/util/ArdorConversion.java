package org.marble.util;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.ardor3d.math.Matrix4;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyMatrix4;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Ardor3D helper conversion methods.
 */
public final class ArdorConversion {
    private ArdorConversion() {
    }

    /**
     * Converts a vecmath 4D matrix into an Ardor3D 4D matrix.
     * 
     * @param from
     *            The input matrix.
     * @param to
     *            The output matrix to store the result in.
     */
    public static void fromMatrix4(final Matrix4d from, final Matrix4 to) {
        to.set(from.m00, from.m01, from.m02, from.m03, from.m10, from.m11,
                from.m12, from.m13, from.m20, from.m21, from.m22, from.m23,
                from.m30, from.m31, from.m32, from.m33);
    }

    /**
     * Converts an Ardor3D 3D matrix into a vecmath 3D matrix.
     * 
     * @param from
     *            The input matrix.
     * @param to
     *            The output matrix to store the result in.
     */
    public static void toMatrix3(final ReadOnlyMatrix3 from, final Matrix3d to) {
        to.m00 = from.getValuef(0, 0);
        to.m01 = from.getValuef(0, 1);
        to.m02 = from.getValuef(0, 2);
        to.m10 = from.getValuef(1, 0);
        to.m11 = from.getValuef(1, 1);
        to.m12 = from.getValuef(1, 2);
        to.m20 = from.getValuef(2, 0);
        to.m21 = from.getValuef(2, 1);
        to.m22 = from.getValuef(2, 2);
    }

    /**
     * Converts an Ardor3D 4D matrix into a vecmath 4D matrix.
     * 
     * @param from
     *            The input matrix.
     * @param to
     *            The output matrix to store the result in.
     */
    public static void toMatrix4(final ReadOnlyMatrix4 from, final Matrix4d to) {
        to.m00 = from.getValuef(0, 0);
        to.m01 = from.getValuef(0, 1);
        to.m02 = from.getValuef(0, 2);
        to.m03 = from.getValuef(0, 3);
        to.m10 = from.getValuef(1, 0);
        to.m11 = from.getValuef(1, 1);
        to.m12 = from.getValuef(1, 2);
        to.m13 = from.getValuef(1, 3);
        to.m20 = from.getValuef(2, 0);
        to.m21 = from.getValuef(2, 1);
        to.m22 = from.getValuef(2, 2);
        to.m22 = from.getValuef(2, 3);
        to.m30 = from.getValuef(3, 0);
        to.m31 = from.getValuef(3, 1);
        to.m32 = from.getValuef(3, 2);
        to.m32 = from.getValuef(3, 3);
    }

    /**
     * Converts an Ardor3D 3D vector into a vecmath 3D vector.
     * 
     * @param from
     *            The input vector.
     * @param to
     *            The output vector to store the result in.
     */
    public static void toVector3(final ReadOnlyVector3 from, final Vector3d to) {
        to.x = from.getXf();
        to.y = from.getYf();
        to.z = from.getZf();
    }
}
