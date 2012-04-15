package org.marble.graphics;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

/**
 * A dynamically sized segmented box that maintains proportionally applied
 * material parameters. When a texture is applied to this box, the border of the
 * texture will be retained as specified, and the center parts of the texture
 * will be repeated according to an algorithm.
 */
public class SegmentedBox extends Mesh {

    /**
     * Creates a new segmented box.
     * 
     * It is recommended to keep {@code borderSize} and {@code middleSize} as
     * close as possible to get a natural-looking object.
     * 
     * @param borderSize
     *            How large, in world space, that the border should be rendered
     *            as.
     * @param middleSize
     *            How large, in world space, that a repeated middle tile should
     *            be. Middle tiles are guaranteed to have at most this size; the
     *            middle space is repartitioned to accommodate more tiles to
     *            ensure this is the case.
     * @param textureBorderSize
     *            A value between 0 and 1 specifying how much of the texture or
     *            applied material to reserve for the static border. A value of
     *            {@code 0.1} would make 2x10% along one axis of the texture be
     *            the border, and 80% be part of the repeated middle.
     */
    public SegmentedBox(final float borderSize, final float middleSize,
            final float textureBorderSize) {
        this(borderSize, middleSize, textureBorderSize, Vector3f.ZERO, 0.5f,
                0.5f, 0.5f);
    }

    /**
     * Creates a new segmented box.
     * 
     * It is recommended to keep {@code borderSize} and {@code middleSize} as
     * close as possible to get a natural-looking object.
     * 
     * @param borderSize
     *            How large, in world space, that the border should be rendered
     *            as.
     * 
     * @param middleSize
     *            How large, in world space, that a repeated middle tile should
     *            be. Middle tiles are guaranteed to have at most this size; the
     *            middle space is repartitioned to accommodate more tiles to
     *            ensure this is the case.
     * 
     * @param textureBorderSize
     *            A value between 0 and 1 specifying how much of the texture or
     *            applied material to reserve for the static border. A value of
     *            {@code 0.1} would make 2x10% along one axis of the texture be
     *            the border, and 80% be part of the repeated middle.
     * 
     * @param center
     *            Where the center of the box should be, in local model space.
     * 
     * @param extentX
     *            How far the box should extend from the center in x-direction.
     * 
     * @param extentY
     *            How far the box should extend from the center in y-direction.
     * 
     * @param extentZ
     *            How far the box should extend from the center in z-direction.
     */
    public SegmentedBox(final float borderSize, final float middleSize,
            final float textureBorderSize, final Vector3f center,
            final float extentX, final float extentY, final float extentZ) {
        super();
        updateGeometry(center, extentX, extentY, extentZ, borderSize,
                middleSize, textureBorderSize);
    }

    private void updateGeometry(final Vector3f center, final float extentX,
            final float extentY, final float extentZ, final float borderSize,
            final float middleSize, final float textureBorderSize) {
        if (!(borderSize > 0 && middleSize > 0 && textureBorderSize > 0 && textureBorderSize < 1))
            return;
        final float texBorderUpper = textureBorderSize;
        final float texBorderLower = 1 - textureBorderSize;

        final float centerX = center.getX();
        final float centerY = center.getY();
        final float centerZ = center.getZ();

        final float north = centerY + extentY;
        final float south = centerY - extentY;
        final float east = centerX + extentX;
        final float west = centerX - extentX;
        final float down = centerZ - extentZ;
        final float up = centerZ + extentZ;

        final float borderNorth = north - borderSize;
        final float borderSouth = south + borderSize;
        final float borderEast = east - borderSize;
        final float borderWest = west + borderSize;

        final float middleSizeX = 2 * (extentX - borderSize);
        final float middleSizeY = 2 * (extentY - borderSize);

        final int segmentsX =
                Math.max(0, (int) Math.ceil(middleSizeX / middleSize));
        final int segmentsY =
                Math.max(0, (int) Math.ceil(middleSizeY / middleSize));

        final float segmentSizeX = middleSizeX / segmentsX;
        final float segmentSizeY = middleSizeY / segmentsY;

        final QuadMeshBuilder builder =
                new QuadMeshBuilder((segmentsX + 4) * (segmentsY + 4) - 3);

        /**
         * Upper face of the box: {@code
         * +-+-+-+-+ <- north
         * |1|2|2|3|    height: border
         * +-+-+-+-+ <- borderNorth
         * |4|5|5|6|
         * +-+-+-+-+    height: segmentSizeY x segmentsY
         * |4|5|5|6|
         * +-+-+-+-+ <- borderSouth
         * |7|8|8|9|    height: border
         * +-+-+-+-+ <- south
         *         ^    east
         *       ^      borderEast
         *   ^          borderWest
         * ^            west
         * }
         * 
         * Texture partition: {@code
         * +-+-+-+ <- 0
         * |1|2|3|
         * +-+-+-+ <- texBorderUpper
         * |4|5|6|
         * +-+-+-+ <- texBorderLower
         * |7|8|9|
         * +-+-+-+ <- 1
         *       ^    1
         *     ^      texBorderLower
         *   ^        texBorderUpper
         * ^          0
         * }
         * 
         * The sides of the box are filled with the row of tiles from the upper
         * face closest to that side, mirrored by the upper side axis.
         * 
         * The bottom of the box is
         * 
         */

        // 1
        builder.addXQuad(false, true, west, north, up, borderNorth, down, 0, 0,
                texBorderUpper, texBorderUpper, -1, 0, 0);
        builder.addYQuad(true, false, north, west, up, borderWest, down, 0, 0,
                texBorderUpper, texBorderUpper, 0, 1, 0);
        builder.addZQuad(false, false, up, west, north, borderWest,
                borderNorth, 0, 0, texBorderUpper, texBorderUpper, 0, 0, 1);

        // 3
        builder.addXQuad(true, true, east, north, up, borderNorth, down, 1, 0,
                texBorderLower, texBorderUpper, 1, 0, 0);
        builder.addYQuad(false, false, north, east, up, borderEast, down, 1, 0,
                texBorderLower, texBorderUpper, 0, 1, 0);
        builder.addZQuad(true, false, up, east, north, borderEast, borderNorth,
                1, 0, texBorderLower, texBorderUpper, 0, 0, 1);

        // 7
        builder.addXQuad(true, true, west, south, up, borderSouth, down, 0, 1,
                texBorderUpper, texBorderLower, -1, 0, 0);
        builder.addYQuad(false, false, south, west, up, borderWest, down, 0, 1,
                texBorderUpper, texBorderLower, 0, -1, 0);
        builder.addZQuad(true, false, up, west, south, borderWest, borderSouth,
                0, 1, texBorderUpper, texBorderLower, 0, 0, 1);

        // 9
        builder.addXQuad(false, true, east, south, up, borderSouth, down, 1, 1,
                texBorderLower, texBorderLower, 1, 0, 0);
        builder.addYQuad(true, false, south, east, up, borderEast, down, 1, 1,
                texBorderLower, texBorderLower, 0, -1, 0);
        builder.addZQuad(false, false, up, east, south, borderEast,
                borderSouth, 1, 1, texBorderLower, texBorderLower, 0, 0, 1);

        for (int segmentY = 0; segmentY < segmentsY; segmentY++) {
            final float segmentPosY = borderSouth + segmentY * segmentSizeY;
            // 4
            builder.addXQuad(false, true, west, segmentPosY + segmentSizeY, up,
                    segmentPosY, down, 0, texBorderUpper, texBorderUpper,
                    texBorderLower, -1, 0, 0);
            builder.addZQuad(true, false, up, west, segmentPosY, borderWest,
                    segmentPosY + segmentSizeY, 0, texBorderLower,
                    texBorderUpper, texBorderUpper, 0, 0, 1);

            // 6
            builder.addXQuad(false, true, east, segmentPosY, up, segmentPosY
                    + segmentSizeY, down, 1, texBorderLower, texBorderLower,
                    texBorderUpper, 1, 0, 0);
            builder.addZQuad(true, false, up, borderEast, segmentPosY, east,
                    segmentPosY + segmentSizeY, texBorderLower, texBorderLower,
                    1, texBorderUpper, 0, 0, 1);
        }

        for (int segmentX = 0; segmentX < segmentsX; segmentX++) {
            final float segmentPosX = borderWest + segmentX * segmentSizeX;
            // 2
            builder.addYQuad(true, false, north, segmentPosX, up, segmentPosX
                    + segmentSizeX, down, texBorderUpper, 0, texBorderLower,
                    texBorderUpper, 0, 1, 0);
            builder.addZQuad(false, false, up, segmentPosX, north, segmentPosX
                    + segmentSizeX, borderNorth, texBorderUpper, 0,
                    texBorderLower, texBorderUpper, 0, 0, 1);

            // 8
            builder.addYQuad(false, false, south, segmentPosX, up, segmentPosX
                    + segmentSizeX, down, texBorderUpper, 1, texBorderLower,
                    texBorderLower, 0, -1, 0);
            builder.addZQuad(false, false, up, segmentPosX, borderSouth,
                    segmentPosX + segmentSizeX, south, texBorderUpper,
                    texBorderLower, texBorderLower, 1, 0, 0, 1);
            for (int segmentY = 0; segmentY < segmentsY; segmentY++) {
                final float segmentPosY = borderSouth + segmentY * segmentSizeY;
                // 5
                builder.addZQuad(true, false, up, segmentPosX, segmentPosY,
                        segmentPosX + segmentSizeX, segmentPosY + segmentSizeY,
                        texBorderUpper, texBorderLower, texBorderLower,
                        texBorderUpper, 0, 0, 1);
            }
        }

        // 5
        builder.addZQuad(true, false, down, west, north, east, south,
                texBorderLower, texBorderLower, texBorderUpper, texBorderUpper,
                0, 0, -1);

        setBuffer(Type.Position, 3,
                BufferUtils.createFloatBuffer(builder.getVertices()));
        setBuffer(Type.Normal, 3,
                BufferUtils.createFloatBuffer(builder.getNormals()));
        setBuffer(Type.TexCoord, 2,
                BufferUtils.createFloatBuffer(builder.getTexcoords()));
        setBuffer(Type.Index, 3,
                BufferUtils.createIntBuffer(builder.getIndices()));
        updateBound();
    }
}
