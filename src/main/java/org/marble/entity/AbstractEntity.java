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
    protected Game game = null;

    public AbstractEntity() {
        centerNode = new Node();
    }

    @Override
    public void destroy() throws Exception {
        // Do nothing
    }

    @Override
    public void die() {
        game.getEntityManager().removeEntity(this);
    }

    @Override
    public String getName() {
        return centerNode.getName();
    }

    public Node getSpatial() {
        return centerNode;
    }

    @Override
    public Transform getTransform() {
        return getSpatial().getLocalTransform();
    }

    @Override
    public void initialize(final Game game) throws Exception {
        this.game = game;
    }

    @Override
    public void setName(final String name) {
        centerNode.setName(name);
    }

    @Override
    public void setTransform(final Transform transform) {
        getSpatial().setLocalTransform(transform);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName()).toString();
    }

    @Override
    public void update(final float tpf) throws Exception {
        // Do nothing
    }
}
