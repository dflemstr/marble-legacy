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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
     *            The size "radius" along the X-axis.
     * @param height
     *            The size "radius" along the Y-axis.
     * @param depth
     *            The size "radius" along the Z-axis.
     */
    public Slab(final Float width, final Float height, final Float depth) {
        this(width, height, depth, 0.0f);
    }

    /**
     * Creates a new slab.
     *
     * @param width
     *            The size "radius" along the X-axis.
     * @param height
     *            The size "radius" along the Y-axis.
     * @param depth
     *            The size "radius" along the Z-axis.
     * @param mass
     *            The mass.
     */
    public Slab(final Float width, final Float height, final Float depth,
            final Float mass) {
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
        // TODO add more connectors
        return ImmutableMap.of("north",
                Connectors.offsetBy(0, depth * 2, 0, 0, 0, 0), "south",
                Connectors.offsetBy(0, -depth * 2, 0, (float) Math.PI, 0, 0));
    }

    @Override
    public Spatial getSpatial() {
        return graphicalBox;
    }

    @Override
    public void initialize() {

        graphicalBox =
                new Box("slab", new Vector3(0, 0, 0), width, height, depth);
        graphicalBox.addController(new EntityController(this));
        graphicalBox.setRandomColors(); // XXX Debug

        final CollisionShape physicalShape =
                new BoxShape(new Vector3f(width, height, depth));
        final Vector3f inertia = new Vector3f(0, 0, 0);
        physicalShape.calculateLocalInertia(mass, inertia);

        final MotionState motionState = new EntityMotionState(this);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo(mass, motionState, physicalShape,
                        inertia);
        physicalBox = new RigidBody(info);
    }
}
