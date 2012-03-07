package org.marble.block;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.marble.entity.AbstractEntity;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;
import org.marble.graphics.EntityController;
import org.marble.physics.EntityMotionState;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Box;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;
import com.google.common.collect.ImmutableSet;

/**
 * A box-shaped block.
 */
public class Slab extends AbstractEntity implements Graphical, Physical {
    private final Box graphicalBox;
    private final RigidBody physicalBox;

    /**
     * Creates a new box.
     * 
     * @param name
     *            The name, for debug purposes.
     * @param transform
     *            The local transform, including translation, rotation and
     *            scale.
     * @param width
     *            The size "radius" along the X-axis.
     * @param height
     *            The size "radius" along the Y-axis.
     * @param depth
     *            The size "radius" along the Z-axis.
     * @param mass
     *            The mass.
     */
    public Slab(final String name, final Matrix4f transform, final float width,
            final float height, final float depth, final float mass) {
        super(transform);
        this.graphicalBox =
                new Box(name, new Vector3(0, 0, 0), width, height, depth);
        this.graphicalBox.addController(new EntityController(this));
        this.graphicalBox.setRandomColors(); // XXX Debug

        final CollisionShape physicalShape =
                new BoxShape(new Vector3f(width, height, depth));
        final Vector3f inertia = new Vector3f(0, 0, 0);
        physicalShape.calculateLocalInertia(mass, inertia);

        final MotionState motionState = new EntityMotionState(this);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo(mass, motionState, physicalShape,
                        inertia);
        this.physicalBox = new RigidBody(info);
    }

    @Override
    public ImmutableSet<ActionInterface> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public RigidBody getBody() {
        return this.physicalBox;
    }

    @Override
    public Spatial getSpatial() {
        return this.graphicalBox;
    }
}
