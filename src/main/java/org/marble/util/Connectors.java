package org.marble.util;

import java.util.Map;

import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import com.google.common.collect.ImmutableMap;

import org.marble.entity.connected.Connector;

public final class Connectors {
    private Connectors() {
    }

    public static ImmutableMap<String, Connector> fromSpiral(
            final float radius, final float height, final float tubeRadius,
            final float separation, final float angle,
            final Vector3f direction, final float a, final float b) {

        final ImmutableMap.Builder<String, Connector> builder =
                ImmutableMap.builder();

        final Vector3f n;
        if (direction.equals(Vector3f.UNIT_X)) {
            n = direction.cross(Vector3f.UNIT_Y);
        } else {
            n = direction.cross(Vector3f.UNIT_X);
        }
        n.normalizeLocal();
        final float pi = (float) Math.PI;

        Vector3f r;
        Vector3f r1;
        Vector3f r2;
        r = n.mult(radius);
        r1 = n.mult(radius - a / 2);
        r1.addLocal(direction.mult(-b / 2));
        r2 = n.mult(radius + a / 2 + tubeRadius);
        r2.addLocal(direction.mult(b / 2));
        builder.put("start_middle",
                offsetBy(r.getX(), r.getY(), r.getZ(), 0, 0, 0));
        builder.put("start_inner",
                offsetBy(r1.getX(), r1.getY(), r1.getZ(), 0, 0, 0));
        builder.put("start_outer",
                offsetBy(r2.getX(), r2.getY(), r2.getZ(), 0, 0, 0));
        final Quaternion rotation = new Quaternion();
        rotation.fromAngleAxis(angle, direction);

        rotation.mult(n, n);
        r = n.mult(radius);
        r.addLocal(direction.mult(height));
        r1 = n.mult(radius - a / 2);
        r1.addLocal(direction.mult(height - b / 2));
        r2 = n.mult(radius + a / 2);
        r2.addLocal(direction.mult(height + b / 2));
        rotation.fromAngleAxis(angle + FastMath.PI, direction);
        builder.put("end_middle",
                offsetBy(r.getX(), r.getY(), r.getZ(), 0, 0, angle + pi));
        builder.put("end_inner",
                offsetBy(r1.getX(), r1.getY(), r1.getZ(), 0, 0, angle + pi));
        builder.put("end_outer",
                offsetBy(r2.getX(), r2.getY(), r2.getZ(), 0, 0, angle + pi));

        return builder.build();
    }

    public static ImmutableMap<String, Connector> fromRail(final float width,
            final float height, final float depth) {

        final ImmutableMap.Builder<String, Connector> builder =
                ImmutableMap.builder();

        final float pi = FastMath.PI;

        builder.put("start_middle", offsetBy(width / 2, 0, 0, 0, 0, 0));
        builder.put("start_right",
                offsetBy(width / 2, (height / 2), 0, 0, 0, 0));
        builder.put("start_left",
                offsetBy(width / 2, -(height / 2), 0, 0, 0, 0));

        builder.put("end_middle", offsetBy(-width / 2, 0, 0, 0, 0, pi));
        builder.put("end_right",
                offsetBy(-width / 2, (height / 2), 0, 0, 0, pi));
        builder.put("end_left",
                offsetBy(-width / 2, -(height / 2), 0, 0, 0, pi));

        builder.put("middle_right",
                offsetBy(0, height / 2 + depth / 2, 0, 0, 0, pi / 2));
        builder.put("middle_left",
                offsetBy(0, -height / 2 - depth / 2, 0, 0, 0, pi / 2));

        return builder.build();
    }

    public static ImmutableMap<String, Connector> fromBox(final float width,
            final float height, final float depth, final float slopeX,
            final float slopeY) {
        final float verticalOffset = 0.5f;
        final int xcount = (int) width, ycount = (int) height, zcount =
                (int) depth;
        final float xborder = (width - xcount + 1) / 2, yborder =
                (height - ycount + 1) / 2, zborder = (depth - zcount + 1) / 2;
        final float xhalf = width / 2, yhalf = height / 2, zhalf = depth / 2;
        final float pi = FastMath.PI;
        final float pihalf = FastMath.HALF_PI;
        final ImmutableMap.Builder<String, Connector> connectorBuilder =
                ImmutableMap.builder();

        connectorBuilder.put("top_middle", offsetBy(0, 0, zhalf, 0, pihalf, 0));
        connectorBuilder.put("bottom_middle",
                offsetBy(0, 0, -zhalf, 0, -pihalf, 0));
        connectorBuilder.put(
                "north_middle",
                offsetBy(0, yhalf, verticalOffset + yhalf * slopeY, 0, 0,
                        pihalf));
        connectorBuilder.put(
                "south_middle",
                offsetBy(0, -yhalf, verticalOffset - yhalf * slopeY, 0, 0,
                        -pihalf));
        for (int x = 0; x < xcount; x++) {
            final float xcoord = xborder + x - xhalf;

            // Top/Bottom
            for (int y = 0; y < ycount; y++) {
                final float ycoord = yborder + y - yhalf;
                connectorBuilder.put(
                        makeConnectorName("top", x, y),
                        offsetBy(xcoord, ycoord, zhalf + xcoord * slopeX
                                + ycoord * slopeY, verticalOffset, pihalf, 0));
                connectorBuilder.put(
                        makeConnectorName("bottom", x, y),
                        offsetBy(xcoord, ycoord, -zhalf + xcoord * slopeX
                                + ycoord * slopeY, verticalOffset, -pihalf, 0));
            }

            // North/South
            for (int z = 0; z < zcount; z++) {
                final float zcoord = zborder + z - zhalf;
                connectorBuilder.put(
                        makeConnectorName("north", x, z),
                        offsetBy(xcoord, yhalf, zcoord + verticalOffset
                                + xcoord * slopeX + yhalf * slopeY, 0, 0,
                                pihalf));
                connectorBuilder.put(
                        makeConnectorName("south", x, z),
                        offsetBy(xcoord, -yhalf, zcoord + verticalOffset
                                + xcoord * slopeX - yhalf * slopeY, 0, 0,
                                -pihalf));
            }
        }

        // East/West
        connectorBuilder.put("east_middle",
                offsetBy(xhalf, 0, verticalOffset + xhalf * slopeX, 0, 0, 0));
        connectorBuilder.put("west_middle",
                offsetBy(-xhalf, 0, verticalOffset - xhalf * slopeX, 0, 0, pi));
        for (int y = 0; y < ycount; y++) {
            final float ycoord = yborder + y - yhalf;
            for (int z = 0; z < zcount; z++) {
                final float zcoord = zborder + z - zhalf;
                connectorBuilder.put(
                        makeConnectorName("east", y, z),
                        offsetBy(xhalf, ycoord, zcoord + verticalOffset
                                + ycoord * slopeY + xhalf * slopeX, 0, 0, 0));
                connectorBuilder.put(
                        makeConnectorName("west", y, z),
                        offsetBy(-xhalf, ycoord, zcoord + verticalOffset
                                + ycoord * slopeY - xhalf * slopeX, 0, 0, pi));
            }
        }

        return connectorBuilder.build();
    }

    public static Connector offsetBy(final Vector3f translation) {
        return offsetBy(translation, Quaternion.ZERO);
    }

    public static Connector offsetBy(final Vector3f translation,
            final Quaternion rotation) {
        return offsetBy(translation, rotation, new Vector3f(1, 1, 1));
    }

    public static Connector offsetBy(final Vector3f translation,
            final Quaternion rotation, final Vector3f scale) {
        final Matrix4f transform = new Matrix4f();

        transform.loadIdentity();
        transform.setRotationQuaternion(rotation);
        transform.setTranslation(translation);
        transform.setScale(scale);

        return new Connector(transform);
    }

    public static Connector
            offsetBy(final float x, final float y, final float z) {
        return offsetBy(x, y, z, 0, 0, 0);
    }

    public static Connector
            offsetBy(final float x, final float y, final float z,
                    final float pitch, final float roll, final float yaw) {
        return offsetBy(x, y, z, pitch, roll, yaw, 1, 1, 1);
    }

    public static Connector offsetBy(final float x, final float y,
            final float z, final float pitch, final float roll,
            final float yaw, final float sx, final float sy, final float sz) {
        return offsetBy(new Vector3f(x, y, z), new Quaternion().fromAngles(
                pitch, roll, yaw), new Vector3f(sx, sy, sz));
    }

    private static String makeConnectorName(final String base,
            final int coord1, final int coord2) {
        return base + "_" + coord1 + "_" + coord2;
    }

    public static Map<String, Connector> fromWall(final float length) {
        final float pi = (float) Math.PI;
        final ImmutableMap.Builder<String, Connector> connectorBuilder =
                ImmutableMap.builder();
        for (int i = 0; i < length; i++) {
            connectorBuilder.put("position_" + i,
                    offsetBy(length / 2 - i - 0.5f, 0, -0.6f, 0, 0, pi / 2));
        }
        connectorBuilder.put("position_middle",
                offsetBy(0, 0, -0.6f, 0, 0, pi / 2));
        return connectorBuilder.build();
    }
}
