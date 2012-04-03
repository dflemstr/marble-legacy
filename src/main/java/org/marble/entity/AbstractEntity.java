package org.marble.entity;

import javax.vecmath.Matrix4d;

import com.google.common.base.Objects;

import org.marble.Game;

/**
 * A default entity implementation with sensible defaults.
 */
public class AbstractEntity implements Entity {
    protected final Matrix4d transform;
    protected String name = "";

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

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", name).toString();
    }
}
