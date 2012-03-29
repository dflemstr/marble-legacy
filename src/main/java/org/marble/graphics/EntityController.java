package org.marble.graphics;

import javax.vecmath.Matrix4d;

import com.ardor3d.math.Matrix4;
import com.ardor3d.math.Transform;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;

import org.marble.entity.Entity;
import org.marble.util.ArdorConversion;

/**
 * A controller that makes the spatial follow an entity.
 */
public class EntityController implements SpatialController<Spatial> {

    private final Entity entity;
    private final Matrix4d matrix = new Matrix4d();
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
        ArdorConversion.fromMatrix4(matrix, graphicalMatrix);
        transform.fromHomogeneousMatrix(graphicalMatrix);
        caller.setTransform(transform);
    }
}
