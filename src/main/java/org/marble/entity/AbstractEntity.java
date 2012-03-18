package org.marble.entity;

import javax.vecmath.Matrix4f;

/**
 * A default entity implementation with sensible defaults.
 */
public abstract class AbstractEntity implements Entity {
    private final Matrix4f transform;

    /**
     * Creates a new entity.
     *
     * @param transform
     *            The initial transform of the entity. This transform will not
     *            be copied before use.
     */
    public AbstractEntity() {
        transform = new Matrix4f();
    }

    @Override
    public Matrix4f getTransform() {
        return transform;
    }

    @Override
    public void setTransform(final Matrix4f transform) {
        this.transform.set(transform);
    }
}
