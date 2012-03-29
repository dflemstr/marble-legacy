package org.marble.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.ardor3d.util.geom.BufferUtils;

/**
 * A builder for constructing mesh data that consists entirely out of quads.
 */
public class QuadVertexBuilder {
    private final FloatBuffer vertices;
    private final FloatBuffer normals;
    private final FloatBuffer texcoords;
    private final IntBuffer indices;
    private int i;

    /**
     * Constructs a new quad vertex builder.
     * 
     * @param quadCount
     *            The number of quads that this builder can maximally store.
     *            Going past this limit yields an exception, and going below
     *            this limit wastes memory.
     */
    public QuadVertexBuilder(final int quadCount) {
        vertices = BufferUtils.createVector3Buffer(quadCount * 4);
        normals = BufferUtils.createVector3Buffer(quadCount * 4);
        texcoords = BufferUtils.createVector2Buffer(quadCount * 4);
        indices = BufferUtils.createIntBuffer(quadCount * 6);
        i = 0;
    }

    /**
     * Adds a quad to this vertex builder. The vertices are specified in the
     * following order:
     * 
     * {@code
     * 1---2
     * |   |
     * |   |
     * 3---4
     * }
     * 
     * @param winding
     *            Whether to generate primitives that stores their vertices
     *            counter-clockwise (true), or clockwise (false).
     * @param flip
     *            Whether to flip the mapped texture along the opposite diagonal
     *            of the quad.
     * @param x1
     *            The first vertex's x coordinate.
     * @param y1
     *            The first vertex's y coordinate.
     * @param z1
     *            The first vertex's z coordinate.
     * @param x2
     *            The second vertex's x coordinate.
     * @param y2
     *            The second vertex's y coordinate.
     * @param z2
     *            The second vertex's z coordinate.
     * @param x3
     *            The third vertex's x coordinate.
     * @param y3
     *            The third vertex's y coordinate.
     * @param z3
     *            The third vertex's z coordinate.
     * @param x4
     *            The fourth vertex's x coordinate.
     * @param y4
     *            The fourth vertex's y coordinate.
     * @param z4
     *            The fourth vertex's z coordinate.
     * @param u1
     *            The first vertex's texture u coordinate.
     * @param v1
     *            The first vertex's texture v coordinate.
     * @param u2
     *            The second vertex's texture u coordinate.
     * @param v2
     *            The second vertex's texture v coordinate.
     * @param u3
     *            The third vertex's texture u coordinate.
     * @param v3
     *            The third vertex's texture v coordinate.
     * @param u4
     *            The fourth vertex's texture u coordinate.
     * @param v4
     *            The fourth vertex's texture v coordinate.
     * @param xn
     *            The normal's x coordinate.
     * @param yn
     *            The normal's y coordinate.
     * @param zn
     *            The normal's z coordinate.
     */
    public void addQuad(final boolean winding, final boolean flip,
            final double x1, final double y1, final double z1, final double x2,
            final double y2, final double z2, final double x3, final double y3,
            final double z3, final double x4, final double y4, final double z4,
            final double u1, final double v1, final double u2, final double v2,
            final double u3, final double v3, final double u4, final double v4,
            final double xn, final double yn, final double zn) {
        vertices.put((float) x1);
        vertices.put((float) y1);
        vertices.put((float) z1);
        vertices.put((float) x2);
        vertices.put((float) y2);
        vertices.put((float) z2);
        vertices.put((float) x3);
        vertices.put((float) y3);
        vertices.put((float) z3);
        vertices.put((float) x4);
        vertices.put((float) y4);
        vertices.put((float) z4);

        normals.put((float) xn);
        normals.put((float) yn);
        normals.put((float) zn);
        normals.put((float) xn);
        normals.put((float) yn);
        normals.put((float) zn);
        normals.put((float) xn);
        normals.put((float) yn);
        normals.put((float) zn);
        normals.put((float) xn);
        normals.put((float) yn);
        normals.put((float) zn);

        if (flip) {
            texcoords.put((float) u1);
            texcoords.put((float) v1);
            texcoords.put((float) u3);
            texcoords.put((float) v3);
            texcoords.put((float) u2);
            texcoords.put((float) v2);
            texcoords.put((float) u4);
            texcoords.put((float) v4);
        } else {
            texcoords.put((float) u1);
            texcoords.put((float) v1);
            texcoords.put((float) u2);
            texcoords.put((float) v2);
            texcoords.put((float) u3);
            texcoords.put((float) v3);
            texcoords.put((float) u4);
            texcoords.put((float) v4);
        }

        if (winding) {
            indices.put(i + 0);
            indices.put(i + 1);
            indices.put(i + 3);
            indices.put(i + 0);
            indices.put(i + 3);
            indices.put(i + 2);
        } else {
            indices.put(i + 0);
            indices.put(i + 3);
            indices.put(i + 1);
            indices.put(i + 0);
            indices.put(i + 2);
            indices.put(i + 3);
        }
        i += 4;
    }

    /**
     * Adds a quad to this vertex builder whose geometrical normal is aligned
     * with the x axis.
     * 
     * @param winding
     *            Whether to generate primitives that stores their vertices
     *            counter-clockwise (true), or clockwise (false).
     * @param flip
     *            Whether to flip the mapped texture along the opposite diagonal
     *            of the quad.
     * @param x
     *            All the vertices' x coordinates.
     * @param y1
     *            The first vertex's y coordinate.
     * @param z1
     *            The first vertex's z coordinate.
     * @param y4
     *            The fourth vertex's y coordinate.
     * @param z4
     *            The fourth vertex's z coordinate.
     * @param u1
     *            The first vertex's texture u coordinate.
     * @param v1
     *            The first vertex's texture v coordinate.
     * @param u4
     *            The fourth vertex's texture u coordinate.
     * @param v4
     *            The fourth vertex's texture v coordinate.
     * @param xn
     *            The normal's x coordinate.
     * @param yn
     *            The normal's y coordinate.
     * @param zn
     *            The normal's z coordinate.
     */
    public void addXQuad(final boolean winding, final boolean flip,
            final double x, final double y1, final double z1, final double y4,
            final double z4, final double u1, final double v1, final double u4,
            final double v4, final double xn, final double yn, final double zn) {
        addQuad(winding, flip, x, y1, z1, x, y4, z1, x, y1, z4, x, y4, z4, u1,
                v1, u4, v1, u1, v4, u4, v4, xn, yn, zn);
    }

    /**
     * Adds a quad to this vertex builder whose geometrical normal is aligned
     * with the y axis.
     * 
     * @param spin
     *            Whether to generate primitives that stores their vertices
     *            counter-clockwise (true), or clockwise (false).
     * @param flip
     *            Whether to flip the mapped texture along the opposite diagonal
     *            of the quad.
     * @param y
     *            All the vertices' y coordinates.
     * @param x1
     *            The first vertex's x coordinate.
     * @param z1
     *            The first vertex's z coordinate.
     * @param x4
     *            The fourth vertex's x coordinate.
     * @param z4
     *            The fourth vertex's z coordinate.
     * @param u1
     *            The first vertex's texture u coordinate.
     * @param v1
     *            The first vertex's texture v coordinate.
     * @param u4
     *            The fourth vertex's texture u coordinate.
     * @param v4
     *            The fourth vertex's texture v coordinate.
     * @param xn
     *            The normal's x coordinate.
     * @param yn
     *            The normal's y coordinate.
     * @param zn
     *            The normal's z coordinate.
     */
    public void addYQuad(final boolean spin, final boolean flip,
            final double y, final double x1, final double z1, final double x4,
            final double z4, final double u1, final double v1, final double u4,
            final double v4, final double xn, final double yn, final double zn) {
        addQuad(spin, flip, x1, y, z1, x4, y, z1, x1, y, z4, x4, y, z4, u1, v1,
                u4, v1, u1, v4, u4, v4, xn, yn, zn);
    }

    /**
     * Adds a quad to this vertex builder whose geometrical normal is aligned
     * with the z axis.
     * 
     * @param winding
     *            Whether to generate primitives that stores their vertices
     *            counter-clockwise (true), or clockwise (false).
     * @param flip
     *            Whether to flip the mapped texture along the opposite diagonal
     *            of the quad.
     * @param z
     *            All the vertices' z coordinates.
     * @param x1
     *            The first vertex's x coordinate.
     * @param y1
     *            The first vertex's y coordinate.
     * @param x4
     *            The fourth vertex's x coordinate.
     * @param y4
     *            The fourth vertex's y coordinate.
     * @param u1
     *            The first vertex's texture u coordinate.
     * @param v1
     *            The first vertex's texture v coordinate.
     * @param u4
     *            The fourth vertex's texture u coordinate.
     * @param v4
     *            The fourth vertex's texture v coordinate.
     * @param xn
     *            The normal's x coordinate.
     * @param yn
     *            The normal's y coordinate.
     * @param zn
     *            The normal's z coordinate.
     */
    public void addZQuad(final boolean winding, final boolean flip,
            final double z, final double x1, final double y1, final double x4,
            final double y4, final double u1, final double v1, final double u4,
            final double v4, final double xn, final double yn, final double zn) {
        addQuad(winding, flip, x1, y1, z, x4, y1, z, x1, y4, z, x4, y4, z, u1,
                v1, u4, v1, u1, v4, u4, v4, xn, yn, zn);
    }

    /**
     * The index buffer that maps primitive vertex indices to stored vertex
     * indices.
     */
    public IntBuffer getIndices() {
        return indices;
    }

    /**
     * The normal buffer that stores normal information per vertex.
     */
    public FloatBuffer getNormals() {
        return normals;
    }

    /**
     * The texture coordinate buffer that stores texture mapping per vertex.
     */
    public FloatBuffer getTexcoords() {
        return texcoords;
    }

    /**
     * The vertex buffer that stores vertex locations per vertex.
     */
    public FloatBuffer getVertices() {
        return vertices;
    }
}
