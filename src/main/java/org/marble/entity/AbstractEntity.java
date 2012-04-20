package org.marble.entity;

import com.jme3.math.Transform;
import com.jme3.scene.Node;

import com.google.common.base.Objects;

import org.marble.Game;

/**
 * A default entity implementation with sensible defaults.
 */
public abstract class AbstractEntity implements Entity {
    private final Node centerNode;

    public AbstractEntity() {
        centerNode = new Node();
    }

    public Node getSpatial() {
        return centerNode;
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    public String getName() {
        return centerNode.getName();
    }

    @Override
    public void initialize(final Game game) {
        // Do nothing
    }

    @Override
    public void setName(final String name) {
        centerNode.setName(name);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName()).toString();
    }

    @Override
    public Transform getTransform() {
        return getSpatial().getLocalTransform();
    }

    @Override
    public void setTransform(final Transform transform) {
        getSpatial().setLocalTransform(transform);
    }
}
