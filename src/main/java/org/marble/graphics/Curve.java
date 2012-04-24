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

public class Curve extends Mesh {

    protected int _circleSamples;

    protected int _radialSamples;

    protected final float _angle;

    protected final float _tubeRadius;

    protected final float _radius;

    protected final float _height;

    /**
     * Constructs a new Torus. Center is the origin, but the Torus may be
     * transformed.
     * 
     * @param circleSamples
     *            The number of samples along the circles.
     * @param radialSamples
     *            The number of samples along the radial.
     * @param tubeRadius
     *            the radius of the torus tube.
     * @param centerRadius
     *            The distance from the center of the torus hole to the center
     *            of the torus tube.
     */
    public Curve(final int circleSamples, final int radialSamples,
            final float radius, final float height, final float angle,
            final float tubeRadius) {
        _circleSamples = circleSamples;
        _radialSamples = radialSamples;
        _radius = radius;
        _tubeRadius = tubeRadius;
        _angle = angle;
        _height = height;

        setGeometryData();
        setIndexData();
        updateBound();
    }

    private void setGeometryData() {
        final int verts = ((_circleSamples + 1) * (_radialSamples + 1));
        final FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(verts);
        final FloatBuffer normalBuffer = BufferUtils.createVector3Buffer(verts);
        final FloatBuffer texcoordBuffer =
                BufferUtils.createVector2Buffer(verts);
        final int i = 0;

        final float currentHeight = 0;
        final Vector3f cylinderMiddle = new Vector3f();

        for (final int circleCount = 0; circleCount < _circleSamples; circleCount++) {
            final float fraction = circleCount / _circleSamples;
            final float angleFraction = fraction * _angle;
            cylinderMiddle.add(FastMath.cos(angleFraction) * _radius,
                    FastMath.sin(angleFraction) * _radius, currentHeight);

            // compute slice vertices with duplication at end point
            final int iSave = i;
            for (int radialCount = 0; radialCount < _radialSamples; radialCount++) {
                final float cosPhi = FastMath.cos(phi);
                final float sinPhi = FastMath.sin(phi);
                tempNormal = M.mult(new Vector3f(cosPhi, 0, sinPhi));

                normalBuffer.put(tempNormal.getX()).put(tempNormal.getY())
                        .put(tempNormal.getZ());

                tempNormal.multLocal(_tubeRadius).addLocal(torusMiddle);
                vertexBuffer.put(tempNormal.getX()).put(tempNormal.getY())
                        .put(tempNormal.getZ());

                texcoordBuffer.put(radialFraction).put(circleFraction);
                i++;
            }

            BufferUtils.copyInternalVector3(vertexBuffer, iSave, i);
            BufferUtils.copyInternalVector3(normalBuffer, iSave, i);

            texcoordBuffer.put(1.0f).put(circleFraction);

            i++;
            currentHeight += _height / _circleSamples;
        }

        // duplicate the cylinder ends to form a torus
        for (int iR = 0; iR <= _radialSamples; iR++, i++) {
            BufferUtils.copyInternalVector3(vertexBuffer, iR, i);
            BufferUtils.copyInternalVector3(normalBuffer, iR, i);
            BufferUtils.copyInternalVector2(texcoordBuffer, iR, i);
            texcoordBuffer.put(i * 2 + 1, 1.0f);
        }

        setBuffer(Type.Position, 3, vertexBuffer);
        setBuffer(Type.Normal, 3, normalBuffer);
        setBuffer(Type.TexCoord, 2, texcoordBuffer);
    }

    private void setIndexData() {
        final int tris = (2 * _circleSamples * _radialSamples);
        final IntBuffer indexBuffer = BufferUtils.createIntBuffer(3 * tris);
        int i;
        // generate connectivity
        int connectionStart = 0;
        for (int circleCount = 0; circleCount < _circleSamples - 1; circleCount++) {
            int i0 = connectionStart;
            int i1 = i0 + 1;
            connectionStart += _radialSamples + 1;
            int i2 = connectionStart;
            int i3 = i2 + 1;
            for (i = 0; i < _radialSamples; i++) {

                indexBuffer.put(i0++);
                indexBuffer.put(i2);
                indexBuffer.put(i1);
                indexBuffer.put(i1++);
                indexBuffer.put(i2++);
                indexBuffer.put(i3++);

            }
        }
        setBuffer(Type.Index, 3, indexBuffer);
    }

}
