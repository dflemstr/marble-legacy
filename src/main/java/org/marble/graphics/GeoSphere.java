/**
 * Copyright (c) 2008-2011 Ardor Labs, Inc.
 * 
 * This file is part of Ardor3D.
 * 
 * Ardor3D is free software: you can redistribute it and/or modify it under the
 * terms of its license which may be found in the accompanying LICENSE file or
 * at <http://www.ardor3d.com/LICENSE>.
 */

package org.marble.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

/**
 * GeoSphere - generate a polygon mesh approximating a sphere by recursive
 * subdivision. First approximation is an octahedron; each level of refinement
 * increases the number of polygons by a factor of 4.
 * <p/>
 * Shared vertices are not retained, so numerical errors may produce cracks
 * between polygons at high subdivision levels.
 * <p/>
 * Initial idea and text from C-Sourcecode by Jon Leech 3/24/89
 * <p/>
 * Ported to jMonkeyEngine by David Flemström 2012-04-19
 */
public class GeoSphere extends Mesh {
    private final int maxlevels;

    private float radius;
    private TextureMode textureMode = TextureMode.Original;
    private boolean usingIcosahedron = true;

    /**
     * @param useIcosahedron
     *            true to start with a 20 triangle mesh, false to start with a 8
     *            triangle mesh
     * @param radius
     *            the radius of this sphere
     * @param maxlevels
     *            an integer >= 1 setting the recursion level
     * @param textureMode
     *            the texture mode to use when generating texture coordinates
     */
    public GeoSphere(final boolean useIcosahedron, final float radius,
            final int maxlevels, final TextureMode textureMode) {
        this.radius = radius;
        this.maxlevels = maxlevels;
        usingIcosahedron = useIcosahedron;
        this.textureMode = textureMode;
        updateGeometry();
    }

    public float getRadius() {
        return radius;
    }

    public TextureMode getTextureMode() {
        return textureMode;
    }

    public boolean isUsingIcosahedron() {
        return usingIcosahedron;
    }

    public void setRadius(final float radius) {
        this.radius = radius;
        updateGeometry();
    }

    public void setTextureMode(final TextureMode textureMode) {
        if (this.textureMode != textureMode) {
            this.textureMode = textureMode;
            updateGeometry();
        }
    }

    private int calculateBorderTriangles(int levels) {
        int current = 108;
        // Pattern starts at 4
        levels -= 4;
        while (levels-- > 0) {
            current = 2 * current + 12;
        }
        return current;
    }

    /**
     * Compute the average of two vectors.
     * 
     * @param a
     *            first vector
     * @param b
     *            second vector
     * @return the average of two points
     */
    private Vector3f createMidpoint(final Vector3f a, final Vector3f b) {
        return new Vector3f((a.getX() + b.getX()) * 0.5f,
                (a.getY() + b.getY()) * 0.5f, (a.getZ() + b.getZ()) * 0.5f);
    }

    private void put(final Vector3f vec) {
        put(vec, false);
    }

    private void put(final Vector3f vec, final boolean begining) {
        final FloatBuffer vertBuf =
                (FloatBuffer) getBuffer(Type.Position).getData();
        vertBuf.put(vec.getX());
        vertBuf.put(vec.getY());
        vertBuf.put(vec.getZ());

        final float length = vec.length();
        final FloatBuffer normBuf =
                (FloatBuffer) getBuffer(Type.Normal).getData();
        final float xNorm = vec.getX() / length;
        normBuf.put(xNorm);
        final float yNorm = vec.getY() / length;
        normBuf.put(yNorm);
        final float zNorm = vec.getZ() / length;
        normBuf.put(zNorm);

        final FloatBuffer texBuf =
                (FloatBuffer) getBuffer(Type.TexCoord).getData();
        if (vec.getX() > 0.0 && vec.getY() == 0.0) {
            if (begining) {
                texBuf.put(0);
            } else {
                texBuf.put(1);
            }
        } else {
            texBuf.put((float) ((Math.atan2(yNorm, xNorm) / (2 * Math.PI) + 1) % 1));
        }

        float vPos = 0;
        switch (textureMode) {
        case Original:
            vPos = .5f * (zNorm + 1);
            break;
        case Projected:
            vPos = FastMath.INV_PI * (FastMath.HALF_PI + FastMath.asin(zNorm));
            break;
        }
        texBuf.put(vPos);
    }

    private void updateGeometry() {
        final int initialTriangleCount = usingIcosahedron ? 20 : 8;
        final int initialVertexCount = usingIcosahedron ? 12 : 6;
        // number of triangles = initialTriangleCount * 4^(maxlevels-1)
        final int tris = initialTriangleCount << (maxlevels - 1) * 2;

        // number of vertBuf = (initialVertexCount + initialTriangleCount*4 +
        // initialTriangleCount*4*4 + ...)
        // = initialTriangleCount*(((4^maxlevels)-1)/(4-1)-1) +
        // initialVertexCount
        final int verts =
                initialTriangleCount
                        * (((1 << maxlevels * 2) - 1) / (4 - 1) - 1)
                        + initialVertexCount
                        + calculateBorderTriangles(maxlevels);

        final FloatBuffer vertBuf = BufferUtils.createVector3Buffer(verts);
        final FloatBuffer normBuf = BufferUtils.createVector3Buffer(verts);
        final FloatBuffer textureBuf = BufferUtils.createVector2Buffer(verts);
        setBuffer(Type.Position, 3, vertBuf);
        setBuffer(Type.Normal, 3, normBuf);
        setBuffer(Type.TexCoord, 2, textureBuf);

        int pos = 0;

        Triangle[] old;
        if (usingIcosahedron) {
            final int[] indices =
                    new int[] { pos + 0, pos + 1, pos + 2, pos + 0, pos + 2,
                            pos + 3, pos + 0, pos + 3, pos + 4, pos + 0,
                            pos + 4, pos + 5, pos + 0, pos + 5, pos + 1,
                            pos + 1, pos + 10, pos + 6, pos + 2, pos + 6,
                            pos + 7, pos + 3, pos + 7, pos + 8, pos + 4,
                            pos + 8, pos + 9, pos + 5, pos + 9, pos + 10,
                            pos + 6, pos + 2, pos + 1, pos + 7, pos + 3,
                            pos + 2, pos + 8, pos + 4, pos + 3, pos + 9,
                            pos + 5, pos + 4, pos + 10, pos + 1, pos + 5,
                            pos + 11, pos + 7, pos + 6, pos + 11, pos + 8,
                            pos + 7, pos + 11, pos + 9, pos + 8, pos + 11,
                            pos + 10, pos + 9, pos + 11, pos + 6, pos + 10 };
            final float y = 0.4472f * radius;
            final float a = 0.8944f * radius;
            final float b = 0.2764f * radius;
            final float c = 0.7236f * radius;
            final float d = 0.8507f * radius;
            final float e = 0.5257f * radius;
            pos++;
            put(new Vector3f(0, radius, 0));
            pos++;
            put(new Vector3f(a, y, 0));
            pos++;
            put(new Vector3f(b, y, -d));
            pos++;
            put(new Vector3f(-c, y, -e));
            pos++;
            put(new Vector3f(-c, y, e));
            pos++;
            put(new Vector3f(b, y, d));
            pos++;
            put(new Vector3f(c, -y, -e));
            pos++;
            put(new Vector3f(-b, -y, -d));
            pos++;
            put(new Vector3f(-a, -y, 0));
            pos++;
            put(new Vector3f(-b, -y, d));
            pos++;
            put(new Vector3f(c, -y, e));
            pos++;
            put(new Vector3f(0, -radius, 0));
            final Triangle[] ikosaedron = new Triangle[indices.length / 3];
            for (int i = 0; i < ikosaedron.length; i++) {
                final Triangle triangle = ikosaedron[i] = new Triangle();
                triangle.pt[0] = indices[i * 3];
                triangle.pt[1] = indices[i * 3 + 1];
                triangle.pt[2] = indices[i * 3 + 2];
            }

            old = ikosaedron;
        } else {
            /* Six equidistant points lying on the unit sphere */
            final Vector3f XPLUS = new Vector3f(radius, 0, 0); /* X */
            final Vector3f XMIN = new Vector3f(-radius, 0, 0); /* -X */
            final Vector3f YPLUS = new Vector3f(0, radius, 0); /* Y */
            final Vector3f YMIN = new Vector3f(0, -radius, 0); /* -Y */
            final Vector3f ZPLUS = new Vector3f(0, 0, radius); /* Z */
            final Vector3f ZMIN = new Vector3f(0, 0, -radius); /* -Z */

            final int xplus = pos++;
            put(XPLUS);
            final int xmin = pos++;
            put(XMIN);
            final int yplus = pos++;
            put(YPLUS);
            final int ymin = pos++;
            put(YMIN);
            final int zplus = pos++;
            put(ZPLUS);
            final int zmin = pos++;
            put(ZMIN);

            final Triangle[] octahedron =
                    new Triangle[] { new Triangle(yplus, zplus, xplus),
                            new Triangle(xmin, zplus, yplus),
                            new Triangle(ymin, zplus, xmin),
                            new Triangle(xplus, zplus, ymin),
                            new Triangle(zmin, yplus, xplus),
                            new Triangle(zmin, xmin, yplus),
                            new Triangle(zmin, ymin, xmin),
                            new Triangle(zmin, xplus, ymin) };

            old = octahedron;
        }

        final Vector3f pt0 = new Vector3f();
        final Vector3f pt1 = new Vector3f();
        final Vector3f pt2 = new Vector3f();

        /* Subdivide each starting triangle (maxlevels - 1) times */
        for (int level = 1; level < maxlevels; level++) {
            /* Allocate a next triangle[] */
            final Triangle[] next = new Triangle[old.length * 4];
            for (int i = 0; i < next.length; i++) {
                next[i] = new Triangle();
            }

            /**
             * Subdivide each polygon in the old approximation and normalize the
             * next points thus generated to lie on the surface of the unit
             * sphere. Each input triangle with vertBuf labeled [0,1,2] as shown
             * below will be turned into four next triangles:
             * 
             * <pre>
             * Make next points
             *   a = (0+2)/2
             *   b = (0+1)/2
             *   c = (1+2)/2
             *   
             * 1   /\   Normalize a, b, c
             *    /  \
             * b /____\ c
             * 
             * Construct next triangles
             * 
             *    /\    /\   [0,b,a] 
             *   /  \  /  \  [b,1,c]
             *  /____\/____\ [a,b,c]
             *  0 a 2 [a,c,2]
             * </pre>
             */
            for (int i = 0; i < old.length; i++) {
                int newi = i * 4;
                final Triangle oldt = old[i];
                Triangle newt = next[newi];

                BufferUtils.populateFromBuffer(pt0, vertBuf, oldt.pt[0]);
                BufferUtils.populateFromBuffer(pt1, vertBuf, oldt.pt[1]);
                BufferUtils.populateFromBuffer(pt2, vertBuf, oldt.pt[2]);
                final Vector3f av =
                        createMidpoint(pt0, pt2).normalizeLocal().multLocal(
                                radius);
                final Vector3f bv =
                        createMidpoint(pt0, pt1).normalizeLocal().multLocal(
                                radius);
                final Vector3f cv =
                        createMidpoint(pt1, pt2).normalizeLocal().multLocal(
                                radius);
                final int a = pos++;
                put(av);
                final int b = pos++;
                put(bv);
                final int c = pos++;
                put(cv);

                newt.pt[0] = oldt.pt[0];
                newt.pt[1] = b;
                newt.pt[2] = a;
                newt = next[++newi];

                newt.pt[0] = b;
                newt.pt[1] = oldt.pt[1];
                newt.pt[2] = c;
                newt = next[++newi];

                newt.pt[0] = a;
                newt.pt[1] = b;
                newt.pt[2] = c;
                newt = next[++newi];

                newt.pt[0] = a;
                newt.pt[1] = c;
                newt.pt[2] = oldt.pt[2];
            }

            /* Continue subdividing next triangles */
            old = next;
        }

        final IntBuffer indexBuffer = BufferUtils.createIntBuffer(tris * 3);
        setBuffer(Type.Index, 3, indexBuffer);

        int carryIntIndex = vertBuf.position() / 3;
        for (final Triangle triangle : old) {
            for (final int aPt : triangle.pt) {
                final Vector3f point = new Vector3f();
                BufferUtils.populateFromBuffer(point, vertBuf, aPt);
                if (point.getX() > 0 && point.getY() == 0) {
                    // Find out which 'y' side the triangle is on
                    final float yCenter =
                            (vertBuf.get(triangle.pt[0] * 3 + 1)
                                    + vertBuf.get(triangle.pt[1] * 3 + 1) + vertBuf
                                    .get(triangle.pt[2] * 3 + 1)) / 3.0f;
                    if (yCenter > 0.0) {
                        put(point, true);
                        indexBuffer.put(carryIntIndex++);
                        continue;
                    }
                }
                indexBuffer.put(aPt);
            }
        }
        updateBound();
    }

    public enum TextureMode {
        Original, Projected;
    }

    static class Triangle {
        int[] pt = new int[3];

        public Triangle() {
        }

        public Triangle(final int pt0, final int pt1, final int pt2) {
            pt[0] = pt0;
            pt[1] = pt1;
            pt[2] = pt2;
        }
    }
}
