package org.marble.util;

import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;

import com.google.common.collect.ImmutableMap;

import org.marble.entity.connected.Connector;

public final class Connectors {
    private Connectors() {
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
                offsetBy(0, yhalf, 0, pihalf, 0, 0));
        connectorBuilder.put("south_middle",
                offsetBy(0, -yhalf, 0, -pihalf, 0, 0));
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
                        offsetBy(xcoord, yhalf, zcoord, pihalf, 0, 0));
                connectorBuilder.put(makeAnchorName("south", x, z),
                        offsetBy(xcoord, -yhalf, zcoord, -pihalf, 0, 0));
            }
        }

        // East/West
        connectorBuilder.put("east_middle", offsetBy(xhalf, 0, 0, 0, 0, 0));
        connectorBuilder.put("west_middle", offsetBy(-xhalf, 0, 0, pi, 0, 0));
        for (int y = 0; y < ycount; y++) {
            final float ycoord = yborder + y - yhalf;
            for (int z = 0; z < zcount; z++) {
                final float zcoord = zborder + z - yhalf;
                connectorBuilder.put(makeAnchorName("east", y, z),
                        offsetBy(xhalf, ycoord, zcoord, 0, 0, 0));
                connectorBuilder.put(makeAnchorName("west", y, z),
                        offsetBy(-xhalf, ycoord, zcoord, pi, 0, 0));
            }
        }

        return connectorBuilder.build();
    }

    public static Connector
            offsetBy(final float x, final float y, final float z,
                    final float yaw, final float pitch, final float roll) {
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
