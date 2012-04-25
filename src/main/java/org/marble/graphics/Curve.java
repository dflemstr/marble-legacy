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
import com.jme3.math.Matrix4f;
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

    protected final Vector3f _direction;

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
            final float tubeRadius, final Vector3f direction) {
        _circleSamples = circleSamples;
        _radialSamples = radialSamples;
        _radius = radius;
        _tubeRadius = tubeRadius;
        _angle = angle;
        _height = height;
        _direction = direction.normalize();

        setGeometryData();
        setIndexData();
        updateBound();
    }

    private void setGeometryData() {
        // allocate vertices
        final int verts = ((_circleSamples + 2) * (_radialSamples + 1));
        final FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(verts);

        // allocate normals if requested
        final FloatBuffer normalBuffer = BufferUtils.createVector3Buffer(verts);

        // allocate texture coordinates
        final FloatBuffer texcoordBuffer =
                BufferUtils.createVector2Buffer(verts);

        // generate geometry
        final float inverseCircleSamples = 1.0f / _circleSamples;
        final float inverseRadialSamples = 1.0f / _radialSamples;
        int i = 0;
        // generate the cylinder itself

        final Matrix4f rotTot = new Matrix4f();
        final Matrix4f rotZ = new Matrix4f();
        rotZ.fromAngleAxis(_angle / _circleSamples, _direction);

        Vector3f radialAxis = new Vector3f();
        final Vector3f torusMiddle = new Vector3f();
        Vector3f tempNormal = new Vector3f();

        final Matrix4f transTot = new Matrix4f();
        transTot.loadIdentity();
        final Matrix4f transDir = new Matrix4f();
        transDir.setTranslation(_direction.mult(_height / (_circleSamples)));

        for (int circleCount = 0; circleCount <= _circleSamples; circleCount++) {

            // compute center point on torus circle at specified angle
            final float circleFraction = circleCount * inverseCircleSamples;
            Vector3f n;
            if (_direction.equals(Vector3f.UNIT_X)) {
                n = _direction.cross(Vector3f.UNIT_Y);
                n.normalizeLocal();
            } else {
                n = _direction.cross(Vector3f.UNIT_X);
                n.normalizeLocal();
            }
            radialAxis = rotTot.mult(n);
            radialAxis.mult(_radius, torusMiddle);
            transTot.translateVect(torusMiddle);

            // compute slice vertices with duplication at end point
            final int iSave = i;
            for (int radialCount = 0; radialCount < _radialSamples; radialCount++) {
                final float radialFraction = radialCount * inverseRadialSamples;
                // in [0,1)
                final float phi = FastMath.TWO_PI * radialFraction;
                final float cosPhi = FastMath.cos(phi);
                final float sinPhi = FastMath.sin(phi);
                tempNormal = rotTot.mult(new Vector3f(0, cosPhi, sinPhi));

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
            transTot.mult(transDir, transTot);
            rotZ.mult(rotTot, rotTot);
            i++;
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
        for (int circleCount = 0; circleCount < _circleSamples; circleCount++) {
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
