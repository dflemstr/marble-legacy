package org.marble.physics;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;

import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

import org.marble.entity.Entity;

/**
 * Links a physical motion state to the state of an entity.
 */
public class EntityMotionState extends MotionState {

    private final Matrix4f floatMatrix = new Matrix4f();
    private final Matrix4d doubleMatrix = new Matrix4d();

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
        doubleMatrix.set(entity.getTransform());
        floatMatrix.set(doubleMatrix);
        out.set(floatMatrix);
        return out;
    }

    @Override
    public void setWorldTransform(final Transform worldTrans) {
        worldTrans.getMatrix(floatMatrix);
        doubleMatrix.set(floatMatrix);
        entity.setTransform(doubleMatrix);
    }
}
