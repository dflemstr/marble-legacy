package org.marble.util;

import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
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

        final float pi = FastMath.PI;

        Vector3f n;
        Vector3f r;
        Vector3f r1;
        Vector3f r2;
        if (direction.equals(Vector3f.UNIT_X)) {
            n = direction.cross(Vector3f.UNIT_Y);
            n.normalizeLocal();
        } else {
            n = direction.cross(Vector3f.UNIT_X);
            n.normalizeLocal();
        }

        r = n.mult(radius);
        r1 = n.mult(radius - a / 2);
        r1.addLocal(direction.mult(-b / 2));
        r2 = n.mult(radius + a / 2);
        r2.addLocal(direction.mult(b / 2));
        builder.put("start_middle",
                offsetBy(r.getX(), r.getY(), r.getZ(), 0, 0, 0));

        final Matrix3f rot = new Matrix3f();
        rot.fromAngleAxis(angle, direction);

        rot.mult(n, n);
        r = n.mult(radius);
        r.addLocal(direction.mult(height));
        r1 = n.mult(radius - a / 2);
        r1.addLocal(direction.mult(-b / 2));
        r2 = n.mult(radius + a / 2);
        r2.addLocal(direction.mult(b / 2));
        builder.put("end_middle",
                offsetBy(r.getX(), r.getY(), r.getZ(), 0, 0, 0));

        return builder.build();
    }

    public static ImmutableMap<String, Connector> fromRail(final float width,
            final float height, final float depth) {

        final ImmutableMap.Builder<String, Connector> builder =
                ImmutableMap.builder();

        final float pi = FastMath.PI;

        builder.put("start_middle", offsetBy(width / 2, 0, 0, 0, 0, 0));

        builder.put("end_middle", offsetBy(-width / 2, 0, 0, 0, 0, pi));

        return builder.build();
    }

    public static ImmutableMap<String, Connector> fromBend(final float width,
            final float height, final float depth, final float angle) {

        final ImmutableMap.Builder<String, Connector> builder =
                ImmutableMap.builder();

        final float pi = FastMath.PI;

        builder.put("start_left",
                offsetBy(width / 2 - height / 2, 0, 0, 0, 0, pi / 2));
        builder.put("start_right",
                offsetBy(width / 2 + height / 2, 0, 0, 0, 0, pi / 2));
        builder.put("end_left",
                offsetBy(width / 2, height / 2 - depth / 2, 0, 0, 0, 0));
        builder.put("end_right",
                offsetBy(width / 2, -(height / 2 - depth / 2), 0, 0, 0, 0));

        builder.put("start_middle", offsetBy(width / 2, 0, 0, -pi / 2, 0, 0));
        builder.put(
                "end_middle",
                offsetBy(FastMath.cos(angle) * (width / 2), FastMath.sin(angle)
                        * (width / 2), 0, pi / 2 + angle, 0, 0));

        return builder.build();
    }

    public static ImmutableMap<String, Connector> fromBox(final float width,
            final float height, final float depth) {

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
        connectorBuilder.put("north_middle",
                offsetBy(0, yhalf, 0, 0, 0, pihalf));
        connectorBuilder.put("south_middle",
                offsetBy(0, -yhalf, 0, 0, 0, -pihalf));
        for (int x = 0; x < xcount; x++) {
            final float xcoord = xborder + x - xhalf;

            // Top/Bottom
            for (int y = 0; y < ycount; y++) {
                final float ycoord = yborder + y - yhalf;
                connectorBuilder.put(makeAnchorName("top", x, y),
                        offsetBy(xcoord, ycoord, zhalf, 0, pihalf, 0));
                connectorBuilder.put(makeAnchorName("bottom", x, y),
                        offsetBy(xcoord, ycoord, -zhalf, 0, -pihalf, 0));
            }

            // North/South
            for (int z = 0; z < zcount; z++) {
                final float zcoord = zborder + z - zhalf;
                connectorBuilder.put(makeAnchorName("north", x, z),
                        offsetBy(xcoord, yhalf, zcoord, 0, 0, pihalf));
                connectorBuilder.put(makeAnchorName("south", x, z),
                        offsetBy(xcoord, -yhalf, zcoord, 0, 0, -pihalf));
            }
        }

        // East/West
        connectorBuilder.put("east_middle", offsetBy(xhalf, 0, 0, 0, 0, 0));
        connectorBuilder.put("west_middle", offsetBy(-xhalf, 0, 0, 0, 0, pi));
        for (int y = 0; y < ycount; y++) {
            final float ycoord = yborder + y - yhalf;
            for (int z = 0; z < zcount; z++) {
                final float zcoord = zborder + z - yhalf;
                connectorBuilder.put(makeAnchorName("east", y, z),
                        offsetBy(xhalf, ycoord, zcoord, 0, 0, 0));
                connectorBuilder.put(makeAnchorName("west", y, z),
                        offsetBy(-xhalf, ycoord, zcoord, 0, 0, pi));
            }
        }

        return connectorBuilder.build();
    }

    public static Connector
            offsetBy(final float x, final float y, final float z,
                    final float pitch, final float roll, final float yaw) {
        final Matrix4f transform = new Matrix4f();

        // TODO make more efficient; we don't need a temp matrix here.
        transform.angleRotation(new Vector3f(pitch * FastMath.RAD_TO_DEG, roll
                * FastMath.RAD_TO_DEG, yaw * FastMath.RAD_TO_DEG));

        // Translation column
        transform.set(0, 3, x);
        transform.set(1, 3, y);
        transform.set(2, 3, z);
        transform.set(3, 3, 1);

        return new Connector(transform);
    }

    private static String makeAnchorName(final String base, final int coord1,
            final int coord2) {
        return base + "_" + coord1 + "_" + coord2;
    }
}
