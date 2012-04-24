package org.marble.util;

import java.util.Map;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;

import com.google.common.base.Objects;

public final class StringRepresentation {
    private StringRepresentation() {
    }

    public static String ofColor(final ColorRGBA color) {
        return Objects.toStringHelper(color).add("r", color.r)
                .add("g", color.g).add("b", color.b).add("a", color.a)
                .toString();
    }

    public static String ofTransform(final Transform transform) {
        return Objects.toStringHelper(transform)
                .add("translation", ofVector3f(transform.getTranslation()))
                .add("rotation", ofQuaternion(transform.getRotation()))
                .add("scale", ofVector3f(transform.getScale())).toString();
    }

    private static String ofQuaternion(final Quaternion quaternion) {
        return Objects.toStringHelper(quaternion).add("x", quaternion.getX())
                .add("y", quaternion.getY()).add("z", quaternion.getZ())
                .add("w", quaternion.getW()).toString();
    }

    public static String ofVector3f(final Vector3f vector3f) {
        return Objects.toStringHelper(vector3f).add("x", vector3f.x)
                .add("y", vector3f.y).add("z", vector3f.z).toString();
    }

    public static String ofStringMap(final Map<String, String> map) {
        final Objects.ToStringHelper helper = Objects.toStringHelper(map);
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            helper.add(entry.getKey(), entry.getValue());
        }
        return helper.toString();
    }
}
