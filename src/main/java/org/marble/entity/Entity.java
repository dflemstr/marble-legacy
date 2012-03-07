package org.marble.entity;

import javax.vecmath.Matrix4f;

/**
 * An entity located somewhere in the simulated world.
 */
public interface Entity {

    /**
     * The current location and rotation of the entity.
     */
    public Matrix4f getTransform();

    /**
     * Sets the location and rotation of the entity.
     */
    public void setTransform(Matrix4f transform);
}
