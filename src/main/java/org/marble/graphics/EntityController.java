package org.marble.graphics;

import javax.vecmath.Matrix4f;

import org.marble.entity.Entity;
import org.marble.util.ArdorMath;

import com.ardor3d.math.Matrix4;
import com.ardor3d.math.Transform;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;

/**
 * A controller that makes the spatial follow an entity.
 */
public class EntityController implements SpatialController<Spatial> {

    private final Entity entity;
    private final Matrix4f matrix = new Matrix4f();
    private final Matrix4 graphicalMatrix = new Matrix4();
    private final Transform transform = new Transform();

    /**
     * Creates a new entity controller.
     * 
     * @param entity
     *            The entity to make spatials follow.
     */
    public EntityController(final Entity entity) {
        this.entity = entity;
    }

    @Override
    public void update(final double time, final Spatial caller) {
        matrix.set(entity.getTransform());
        ArdorMath.fromMatrix4f(matrix, graphicalMatrix);
        transform.fromHomogeneousMatrix(graphicalMatrix);
        caller.setTransform(transform);
    }
}
