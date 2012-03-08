package org.marble.physics;

import javax.vecmath.Matrix4f;

import org.marble.entity.Entity;

import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

/**
 * Links a physical motion state to the state of an entity.
 */
public class EntityMotionState extends MotionState {

    private final Matrix4f matrix = new Matrix4f();

    private final Entity entity;

    /**
     * Creates a new entity-linked motion state.
     * 
     * @param entity
     *            The entity to link.
     */
    public EntityMotionState(final Entity entity) {
        this.entity = entity;
    }

    @Override
    public Transform getWorldTransform(final Transform out) {
        out.set(entity.getTransform());
        return out;
    }

    @Override
    public void setWorldTransform(final Transform worldTrans) {
        worldTrans.getMatrix(matrix);
        entity.setTransform(matrix);
    }
}
