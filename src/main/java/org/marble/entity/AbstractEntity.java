package org.marble.entity;

import javax.vecmath.Matrix4d;

import org.marble.Game;

/**
 * A default entity implementation with sensible defaults.
 */
public abstract class AbstractEntity implements Entity {
    private final Matrix4d transform;

    /**
     * Creates a new entity.
     * 
     * @param transform
     *            The initial transform of the entity. This transform will not
     *            be copied before use.
     */
    public AbstractEntity() {
        transform = new Matrix4d();
    }

    @Override
    public Matrix4d getTransform() {
        return transform;
    }

    @Override
    public void initialize(final Game game) {
    }

    @Override
    public void setTransform(final Matrix4d transform) {
        this.transform.set(transform);
    }
}
