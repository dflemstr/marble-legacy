package org.marble.block;

import java.util.Map;

import javax.vecmath.Vector3f;

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

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.Connectivity;
import org.marble.entity.Connector;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;
import org.marble.graphics.EntityController;
import org.marble.physics.EntityMotionState;
import org.marble.util.Connectors;

/**
 * A box-shaped block.
 */
public class Slab extends AbstractEntity implements Connectivity, Graphical,
        Physical {
    private final float width, height, depth;
    private final float mass;
    private Box graphicalBox;
    private RigidBody physicalBox;

    /**
     * Creates a new slab.
     *
     * @param width
     *            The size along the X-axis.
     * @param height
     *            The size along the Y-axis.
     * @param depth
     *            The size along the Z-axis.
     */
    public Slab(final float width, final float height, final float depth) {
        this(width, height, depth, 0.0f);
    }

    /**
     * Creates a new slab.
     *
     * @param width
     *            The size along the X-axis.
     * @param height
     *            The size along the Y-axis.
     * @param depth
     *            The size along the Z-axis.
     * @param mass
     *            The mass.
     */
    public Slab(final float width, final float height, final float depth,
            final float mass) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.mass = mass;
    }

    @Override
    public ImmutableSet<ActionInterface> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public RigidBody getBody() {
        return physicalBox;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return Connectors.fromBox(width, height, depth);
    }

    @Override
    public Spatial getSpatial() {
        return graphicalBox;
    }

    @Override
    public void initialize(final Game game) {
        graphicalBox =
                new Box("slab", new Vector3(0, 0, 0), width / 2, height / 2,
                        depth / 2);
        graphicalBox.addController(new EntityController(this));

        final CollisionShape physicalShape =
                new BoxShape(new Vector3f(width / 2, height / 2, depth / 2));
        final Vector3f inertia = new Vector3f(0, 0, 0);
        physicalShape.calculateLocalInertia(mass, inertia);

        final MotionState motionState = new EntityMotionState(this);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo(mass, motionState, physicalShape,
                        inertia);
        physicalBox = new RigidBody(info);
    }
}
