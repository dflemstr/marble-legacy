package org.marble.util;

import javax.vecmath.Matrix4d;

import com.google.common.collect.ImmutableMap;

import org.marble.entity.Connector;

public final class Connectors {
    private Connectors() {
    }

    public static ImmutableMap<String, Connector> fromSpiral(
            final double width, final double height, final double depth,
            final double angle) {

        final ImmutableMap.Builder<String, Connector> builder =
                ImmutableMap.builder();

        final double pi = Math.PI;

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
                offsetBy(Math.cos(pi - angle) * (width / 2),
                        Math.sin(pi - angle) * (width / 2), angle, -pi / 2
                                - angle, 0, 0));

        return builder.build();
    }

    public static ImmutableMap<String, Connector> fromRail(final double width,
            final double height, final double depth) {

        final ImmutableMap.Builder<String, Connector> builder =
                ImmutableMap.builder();

        final double pi = Math.PI;

        builder.put("start_left",
                offsetBy(-width / 2, height / 2 - depth / 2, 0, 0, 0, 0));
        builder.put("start_right",
                offsetBy(-width / 2, -(height / 2 - depth / 2), 0, 0, 0, 0));
        builder.put("end_left",
                offsetBy(width / 2, height / 2 - depth / 2, 0, 0, 0, 0));
        builder.put("end_right",
                offsetBy(width / 2, -(height / 2 - depth / 2), 0, 0, 0, 0));

        builder.put("start_middle", offsetBy(-width / 2, 0, 0, 0, 0, pi));
        builder.put("end_middle", offsetBy(width / 2, 0, 0, 0, 0, 0));

        return builder.build();
    }

    public static ImmutableMap<String, Connector> fromBend(final double width,
            final double height, final double depth, final double angle) {

        final ImmutableMap.Builder<String, Connector> builder =
                ImmutableMap.builder();

        final double pi = Math.PI;

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
                offsetBy(Math.cos(pi - angle) * (width / 2),
                        Math.sin(pi - angle) * (width / 2), 0, -pi / 2 - angle,
                        0, 0));

        return builder.build();
    }

    public static ImmutableMap<String, Connector> fromBox(final double width,
            final double height, final double depth) {

        final int xcount = (int) width, ycount = (int) height, zcount =
                (int) depth;
        final double xborder = (width - xcount + 1) / 2, yborder =
                (height - ycount + 1) / 2, zborder = (depth - zcount + 1) / 2;
        final double xhalf = width / 2, yhalf = height / 2, zhalf = depth / 2;
        final double pi = Math.PI;
        final double pihalf = pi / 2;
        final ImmutableMap.Builder<String, Connector> connectorBuilder =
                ImmutableMap.builder();

        connectorBuilder.put("top_middle", offsetBy(0, 0, zhalf, 0, pihalf, 0));
        connectorBuilder.put("bottom_middle",
                offsetBy(0, 0, -zhalf, 0, -pihalf, 0));
        connectorBuilder.put("north_middle",
                offsetBy(0, yhalf, 0, pihalf, 0, 0));
        connectorBuilder.put("south_middle",
                offsetBy(0, -yhalf, 0, -pihalf, 0, 0));
        for (int x = 0; x < xcount; x++) {
            final double xcoord = xborder + x - xhalf;

            // Top/Bottom
            for (int y = 0; y < ycount; y++) {
                final double ycoord = yborder + y - yhalf;
                connectorBuilder.put(makeAnchorName("top", x, y),
                        offsetBy(xcoord, ycoord, zhalf, 0, pihalf, 0));
                connectorBuilder.put(makeAnchorName("bottom", x, y),
                        offsetBy(xcoord, ycoord, -zhalf, 0, -pihalf, 0));
            }

            // North/South
            for (int z = 0; z < zcount; z++) {
                final double zcoord = zborder + z - zhalf;
                connectorBuilder.put(makeAnchorName("north", x, z),
                        offsetBy(xcoord, yhalf, zcoord, pihalf, 0, 0));
                connectorBuilder.put(makeAnchorName("south", x, z),
                        offsetBy(xcoord, -yhalf, zcoord, -pihalf, 0, 0));
            }
        }

        // East/West
        connectorBuilder.put("east_middle", offsetBy(xhalf, 0, 0, 0, 0, 0));
        connectorBuilder.put("west_middle", offsetBy(-xhalf, 0, 0, pi, 0, 0));
        for (int y = 0; y < ycount; y++) {
            final double ycoord = yborder + y - yhalf;
            for (int z = 0; z < zcount; z++) {
                final double zcoord = zborder + z - yhalf;
                connectorBuilder.put(makeAnchorName("east", y, z),
                        offsetBy(xhalf, ycoord, zcoord, 0, 0, 0));
                connectorBuilder.put(makeAnchorName("west", y, z),
                        offsetBy(-xhalf, ycoord, zcoord, pi, 0, 0));
            }
        }

        return connectorBuilder.build();
    }

    public static Connector offsetBy(final double x, final double y,
            final double z, final double yaw, final double pitch,
            final double roll) {
        final Matrix4d rotation = new Matrix4d();
        final Matrix4d transform = new Matrix4d();

        // TODO make more efficient; we don't need a temp matrix here.
        transform.setIdentity();
        rotation.rotZ(yaw);
        transform.mul(rotation);
        rotation.rotX(pitch);
        transform.mul(rotation);
        rotation.rotY(roll);
        transform.mul(rotation);

        // Translation column
        transform.setElement(0, 3, x);
        transform.setElement(1, 3, y);
        transform.setElement(2, 3, z);
        transform.setElement(3, 3, 1);

        return new Connector(transform);
    }

    private static String makeAnchorName(final String base, final int coord1,
            final int coord2) {
        return base + "_" + coord1 + "_" + coord2;
    }
}
