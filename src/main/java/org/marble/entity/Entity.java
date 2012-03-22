package org.marble.entity;

import javax.vecmath.Matrix4f;

import org.marble.Game;

/**
 * An entity located somewhere in the simulated world.
 *
 * A class implementing this interface must have one or more public constructors
 * that take zero or more arguments of the types
 * {@code [String, Vector3f, Float]} that will be called when the entity is
 * loaded.
 */
public interface Entity {
    /**
     * The current location and rotation of the entity.
     */
    public Matrix4f getTransform();

    /**
     * Performs allocation of resources. This method is called when the entity
     * is inserted into the world but before it is added to any engines.
     */
    public void initialize(Game game);

    /**
     * Sets the location and rotation of the entity.
     */
    public void setTransform(Matrix4f transform);
}
