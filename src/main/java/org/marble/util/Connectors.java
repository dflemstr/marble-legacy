package org.marble.util;

import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableMap;

import org.marble.entity.Connector;

public final class Connectors {
    public static ImmutableMap<String, Connector> fromBox(final float width,
            final float height, final float depth) {

        final int xcount = (int) width, ycount = (int) height, zcount =
                (int) depth;
        final float xborder = (width - xcount + 1) / 2, yborder =
                (height - ycount + 1) / 2, zborder = (depth - zcount + 1) / 2;
        final float xhalf = width / 2, yhalf = height / 2, zhalf = depth / 2;
        final float pi = (float) Math.PI;
        final ImmutableMap.Builder<String, Connector> connectorBuilder =
                ImmutableMap.builder();

        connectorBuilder.put("top_middle", offsetBy(0, 0, zhalf, 0, pi, 0));
        connectorBuilder
                .put("bottom_middle", offsetBy(0, 0, -zhalf, 0, -pi, 0));
        connectorBuilder.put("north_middle", offsetBy(0, yhalf, 0, pi, 0, 0));
        connectorBuilder.put("south_middle", offsetBy(0, -yhalf, 0, -pi, 0, 0));
        for (int x = 0; x < xcount; x++) {
            final float xcoord = xborder + x - xhalf;

            // Top/Bottom
            for (int y = 0; y < ycount; y++) {
                final float ycoord = yborder + y - yhalf;
                connectorBuilder.put(makeAnchorName("top", x, y),
                        offsetBy(xcoord, ycoord, zhalf, 0, pi, 0));
                connectorBuilder.put(makeAnchorName("bottom", x, y),
                        offsetBy(xcoord, ycoord, -zhalf, 0, -pi, 0));
            }

            // North/South
            for (int z = 0; z < zcount; z++) {
                final float zcoord = zborder + z - zhalf;
                connectorBuilder.put(makeAnchorName("north", x, z),
                        offsetBy(xcoord, yhalf, zcoord, pi, 0, 0));
                connectorBuilder.put(makeAnchorName("south", x, z),
                        offsetBy(xcoord, -yhalf, zcoord, -pi, 0, 0));
            }
        }

        // East/West
        connectorBuilder.put("east_middle", offsetBy(xhalf, 0, 0, 0, 0, 0));
        connectorBuilder.put("west_middle",
                offsetBy(-xhalf, 0, 0, 2 * pi, 0, 0));
        for (int y = 0; y < ycount; y++) {
            final float ycoord = yborder + y - yhalf;
            for (int z = 0; z < zcount; z++) {
                final float zcoord = zborder + z - yhalf;
                connectorBuilder.put(makeAnchorName("east", y, z),
                        offsetBy(xhalf, ycoord, zcoord, 0, 0, 0));
                connectorBuilder.put(makeAnchorName("west", y, z),
                        offsetBy(-xhalf, ycoord, zcoord, 2 * pi, 0, 0));
            }
        }

        final ImmutableMap<String, Connector> connectors =
                connectorBuilder.build();
        System.out.println("box_" + width + "_" + height + "_" + depth + ":\n"
                + connectors);
        return connectors;
    }

    private static String makeAnchorName(final String base, final int coord1,
            final int coord2) {
        return base + "_" + coord1 + "_" + coord2;
    }

    public static Connector
            offsetBy(final float x, final float y, final float z,
                    final float yaw, final float pitch, final float roll) {
        final Matrix4f rotation = new Matrix4f();
        final Matrix4f transform = new Matrix4f();

        // TODO make more efficient; we don't need a temp matrix here.
        transform.setIdentity();
        rotation.rotZ(yaw);
        transform.mul(rotation);
        rotation.rotX(pitch);
        transform.mul(rotation);
        rotation.rotY(roll);
        transform.mul(rotation);

        /**
         * XXX This calculation is buggy; yaw=2pi does for example collapse the
         * matrix {@code
         * final double sinPitch = sin(pitch);
         * final double cosPitch = cos(pitch);
         * final double sinRoll = sin(roll);
         * final double cosRoll = cos(roll);
         * final double sinYaw = sin(yaw);
         * final double cosYaw = cos(yaw);
         * transform.setElement(0, 0, (float) (cosPitch * cosYaw));
         * transform.setElement(0, 1, (float) (-sinYaw * cosPitch));
         * transform.setElement(0, 2, (float) sinPitch); transform.setElement(1,
         * 0, (float) (cosYaw * sinPitch * sinRoll + sinYaw * cosRoll));
         * transform.setElement(1, 1, (float) (-sinYaw * sinPitch * sinRoll +
         * cosYaw * cosRoll)); transform.setElement(1, 2, (float) (-cosPitch *
         * sinRoll)); transform.setElement(2, 0, (float) (-cosYaw * sinPitch *
         * cosRoll + sinYaw * sinRoll)); transform.setElement(2, 1, (float)
         * (sinYaw * sinPitch * cosRoll + cosYaw * sinRoll));
         * transform.setElement(2, 2, (float) (cosPitch * cosRoll));
         * }
         */

        // Translation column
        transform.setElement(0, 3, x);
        transform.setElement(1, 3, y);
        transform.setElement(2, 3, z);
        transform.setElement(3, 3, 1);

        return new Connector(transform);
    }

    private Connectors() {
    }
}
