package org.marble.util;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.ardor3d.math.Matrix4;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyMatrix4;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Ardor3D helper conversion methods.
 */
public final class ArdorMath {
    /**
     * Converts a vecmath 4D matrix into an Ardor3D 4D matrix.
     * 
     * @param matrix
     *            The input matrix.
     * @param out
     *            The output matrix to store the result in.
     */
    public static void fromMatrix4f(final Matrix4f matrix, final Matrix4 out) {
        out.set(matrix.m00, matrix.m01, matrix.m02, matrix.m03, matrix.m10,
                matrix.m11, matrix.m12, matrix.m13, matrix.m20, matrix.m21,
                matrix.m22, matrix.m23, matrix.m30, matrix.m31, matrix.m32,
                matrix.m33);
    }

    /**
     * Converts an Ardor3D 3D matrix into a vecmath 3D matrix.
     * 
     * @param matrix
     *            The input matrix.
     * @param out
     *            The output matrix to store the result in.
     */
    public static void toMatrix3f(final ReadOnlyMatrix3 matrix,
            final Matrix3f out) {
        out.m00 = matrix.getValuef(0, 0);
        out.m01 = matrix.getValuef(0, 1);
        out.m02 = matrix.getValuef(0, 2);
        out.m10 = matrix.getValuef(1, 0);
        out.m11 = matrix.getValuef(1, 1);
        out.m12 = matrix.getValuef(1, 2);
        out.m20 = matrix.getValuef(2, 0);
        out.m21 = matrix.getValuef(2, 1);
        out.m22 = matrix.getValuef(2, 2);
    }

    /**
     * Converts an Ardor3D 4D matrix into a vecmath 4D matrix.
     * 
     * @param matrix
     *            The input matrix.
     * @param out
     *            The output matrix to store the result in.
     */
    public static void toMatrix4f(final ReadOnlyMatrix4 matrix,
            final Matrix4f out) {
        out.m00 = matrix.getValuef(0, 0);
        out.m01 = matrix.getValuef(0, 1);
        out.m02 = matrix.getValuef(0, 2);
        out.m03 = matrix.getValuef(0, 3);
        out.m10 = matrix.getValuef(1, 0);
        out.m11 = matrix.getValuef(1, 1);
        out.m12 = matrix.getValuef(1, 2);
        out.m13 = matrix.getValuef(1, 3);
        out.m20 = matrix.getValuef(2, 0);
        out.m21 = matrix.getValuef(2, 1);
        out.m22 = matrix.getValuef(2, 2);
        out.m22 = matrix.getValuef(2, 3);
        out.m30 = matrix.getValuef(3, 0);
        out.m31 = matrix.getValuef(3, 1);
        out.m32 = matrix.getValuef(3, 2);
        out.m32 = matrix.getValuef(3, 3);
    }

    /**
     * Converts an Ardor3D 3D vector into a vecmath 3D vector.
     * 
     * @param vector
     *            The input vector.
     * @param out
     *            The output vector to store the result in.
     */
    public static void toVector3f(final ReadOnlyVector3 vector,
            final Vector3f out) {
        out.x = vector.getXf();
        out.y = vector.getYf();
        out.z = vector.getZf();
    }

    private ArdorMath() {
    }
}
