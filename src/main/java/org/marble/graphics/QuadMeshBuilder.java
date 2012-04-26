package org.marble.graphics;

import java.util.ArrayList;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

/**
 * A builder for constructing mesh data that consists entirely out of quads.
 */
public class QuadMeshBuilder {
    private final ArrayList<Vector3f> vertices;
    private final ArrayList<Vector3f> normals;
    private final ArrayList<Vector2f> texcoords;
    private final ArrayList<Integer> indices;
    private int i;

    /**
     * Constructs a new quad vertex builder.
     * 
     * @param quadCount
     *            The number of quads that this builder might store. Going above
     *            this number leads to memory allocation, and going below it
     *            leads to wasted memory.
     */
    public QuadMeshBuilder(final int quadCount) {
        vertices = Lists.newArrayListWithExpectedSize(quadCount * 4);
        normals = Lists.newArrayListWithExpectedSize(quadCount * 4);
        texcoords = Lists.newArrayListWithExpectedSize(quadCount * 4);
        indices = Lists.newArrayListWithExpectedSize(quadCount * 6);
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
            final float x1, final float y1, final float z1, final float x2,
            final float y2, final float z2, final float x3, final float y3,
            final float z3, final float x4, final float y4, final float z4,
            final float u1, final float v1, final float u2, final float v2,
            final float u3, final float v3, final float u4, final float v4,
            final float xn, final float yn, final float zn) {
        vertices.add(new Vector3f(x1, y1, z1));
        vertices.add(new Vector3f(x2, y2, z2));
        vertices.add(new Vector3f(x3, y3, z3));
        vertices.add(new Vector3f(x4, y4, z4));

        final Vector3f normal = new Vector3f(xn, yn, zn);
        normals.add(normal);
        normals.add(normal);
        normals.add(normal);
        normals.add(normal);

        if (flip) {
            texcoords.add(new Vector2f(u1, v1));
            texcoords.add(new Vector2f(u3, v3));
            texcoords.add(new Vector2f(u2, v2));
            texcoords.add(new Vector2f(u4, v4));
        } else {
            texcoords.add(new Vector2f(u1, v1));
            texcoords.add(new Vector2f(u2, v2));
            texcoords.add(new Vector2f(u3, v3));
            texcoords.add(new Vector2f(u4, v4));
        }

        if (winding) {
            indices.add(i + 0);
            indices.add(i + 1);
            indices.add(i + 3);
            indices.add(i + 0);
            indices.add(i + 3);
            indices.add(i + 2);
        } else {
            indices.add(i + 0);
            indices.add(i + 3);
            indices.add(i + 1);
            indices.add(i + 0);
            indices.add(i + 2);
            indices.add(i + 3);
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
            final float x, final float y1, final float z1, final float y4,
            final float z4, final float u1, final float v1, final float u4,
            final float v4, final float xn, final float yn, final float zn) {
        addQuad(winding, flip, x, y1, z1, x, y4, z1, x, y1, z4, x, y4, z4, u1,
                v1, u4, v1, u1, v4, u4, v4, xn, yn, zn);
    }

    public void addXQuadSlope(final boolean winding, final boolean flip,
            final float x, final float y1, final float z1, final float y4,
            final float z4, final float u1, final float v1, final float u4,
            final float v4, final float xn, final float yn, final float zn,
            final float slopeX, final float slopeY) {
        addQuad(winding, flip, x, y1, z1, x, y4, z1 + slopeY * (y4 - y1), x,
                y1, z4, x, y4, z4 + slopeY * (y4 - y1), u1, v1, u4, v1, u1, v4,
                u4, v4, xn, yn, zn);
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
    public void addYQuad(final boolean spin, final boolean flip, final float y,
            final float x1, final float z1, final float x4, final float z4,
            final float u1, final float v1, final float u4, final float v4,
            final float xn, final float yn, final float zn) {
        addQuad(spin, flip, x1, y, z1, x4, y, z1, x1, y, z4, x4, y, z4, u1, v1,
                u4, v1, u1, v4, u4, v4, xn, yn, zn);
    }

    public void addYQuadSlope(final boolean spin, final boolean flip,
            final float y, final float x1, final float z1, final float x4,
            final float z4, final float u1, final float v1, final float u4,
            final float v4, final float xn, final float yn, final float zn,
            final float slopeX, final float slopeY) {

        addQuad(spin, flip, x1, y, z1 + slopeX * (x4 - x1), x4, y, z1, x1, y,
                z4 + slopeX * (x4 - x1), x4, y, z4, u1, v1, u4, v1, u1, v4, u4,
                v4, xn, yn, zn);
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
            final float z, final float x1, final float y1, final float x4,
            final float y4, final float u1, final float v1, final float u4,
            final float v4, final float xn, final float yn, final float zn) {
        addQuad(winding, flip, x1, y1, z, x4, y1, z, x1, y4, z, x4, y4, z, u1,
                v1, u4, v1, u1, v4, u4, v4, xn, yn, zn);
    }

    public void addZQuadSlope(final boolean winding, final boolean flip,
            final float z, final float x1, final float y1, final float x4,
            final float y4, final float u1, final float v1, final float u4,
            final float v4, final float xn, final float yn, final float zn,
            final float slopeX, final float slopeY) {
        addQuad(winding, flip, x1, y1, z, x4, y1, z + slopeX * (x4 - x1), x1,
                y4, z + slopeY * (y4 - y1), x4, y4, z + slopeX * (x4 - x1)
                        + slopeY * (y4 - y1), u1, v1, u4, v1, u1, v4, u4, v4,
                xn, yn, zn);
    }

    /**
     * The index buffer that maps primitive vertex indices to stored vertex
     * indices.
     */
    public int[] getIndices() {
        return Ints.toArray(indices);
    }

    /**
     * The normal buffer that stores normal information per vertex.
     */
    public Vector3f[] getNormals() {
        return normals.toArray(new Vector3f[0]);
    }

    /**
     * The texture coordinate buffer that stores texture mapping per vertex.
     */
    public Vector2f[] getTexcoords() {
        return texcoords.toArray(new Vector2f[0]);
    }

    /**
     * The vertex buffer that stores vertex locations per vertex.
     */
    public Vector3f[] getVertices() {
        return vertices.toArray(new Vector3f[0]);
    }
}
