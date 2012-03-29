package org.marble.util;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;

public class JinngineConversion {
    public static void fromMatrix4(final Matrix4d from, final Matrix4 to) {
        to.a11 = from.m00;
        to.a12 = from.m01;
        to.a13 = from.m02;
        to.a14 = from.m03;
        to.a21 = from.m10;
        to.a22 = from.m11;
        to.a23 = from.m12;
        to.a24 = from.m13;
        to.a31 = from.m20;
        to.a32 = from.m21;
        to.a33 = from.m22;
        to.a34 = from.m23;
        to.a41 = from.m30;
        to.a42 = from.m31;
        to.a43 = from.m32;
        to.a44 = from.m33;
    }

    public static void fromVector3(final Vector3d from, final Vector3 to) {
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
    }

    public static void rotFromMatrix4(final Matrix4d from, final Matrix3 to) {
        to.a11 = from.m00;
        to.a12 = from.m01;
        to.a13 = from.m02;
        to.a21 = from.m10;
        to.a22 = from.m11;
        to.a23 = from.m12;
        to.a31 = from.m20;
        to.a32 = from.m21;
        to.a33 = from.m22;
    }

    public static void transFromMatrix4(final Matrix4d from, final Vector3 to) {
        to.x = from.m03;
        to.y = from.m13;
        to.z = from.m23;
    }

    public static void toMatrix4(final Matrix4 from, final Matrix4d to) {
        to.m00 = from.a11;
        to.m01 = from.a12;
        to.m02 = from.a13;
        to.m03 = from.a14;
        to.m10 = from.a21;
        to.m11 = from.a22;
        to.m12 = from.a23;
        to.m13 = from.a24;
        to.m20 = from.a31;
        to.m21 = from.a32;
        to.m22 = from.a33;
        to.m23 = from.a34;
        to.m30 = from.a41;
        to.m31 = from.a42;
        to.m32 = from.a43;
        to.m33 = from.a44;
    }
}
