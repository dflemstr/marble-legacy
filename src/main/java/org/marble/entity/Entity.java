package org.marble.entity;

import javax.vecmath.Matrix4d;

import org.marble.Game;

/**
 * An entity located somewhere in the simulated world.
 * 
 * A class implementing this interface must have one or more public constructors
 * that take zero or more arguments of the types
 * {@code [String, Vector3d, double]} that will be called when the entity is
 * loaded.
 */
public interface Entity {
    /**
     * The current location and rotation of the entity.
     */
    public Matrix4d getTransform();

    /**
     * Performs allocation of resources. This method is called when the entity
     * is inserted into the world but before it is added to any engines.
     */
    public void initialize(Game game);

    /**
     * Destroys resources that were allocated in the {@link #initialize(Game)}
     * call.
     */
    public void destroy();

    /**
     * Sets the location and rotation of the entity.
     */
    public void setTransform(Matrix4d transform);

    /**
     * Sets the debug name of this entity.
     */
    public void setName(String name);

    /**
     * The debug name of this entity.
     */
    public String getName();
}
