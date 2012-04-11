package org.marble.block;

import java.util.Map;

import javax.vecmath.Vector3f;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Box;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import org.marble.entity.AbstractEntity;
import org.marble.entity.Connected;
import org.marble.entity.Connector;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;
import org.marble.graphics.SegmentedBox;
import org.marble.util.Connectors;

/**
 * A box-shaped block.
 */
public class Slab extends AbstractEntity implements Connected, Graphical,
        Physical {
    private final double width, height, depth;
    private final Box graphicalBox;
    private final RigidBody physicalBox;

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
    public Slab(final double width, final double height, final double depth) {
        this(width, height, depth, Optional.<Double> absent());
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
    public Slab(final double width, final double height, final double depth,
            final double mass) {
        this(width, height, depth, Optional.of(mass));
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
    public Slab(final double width, final double height, final double depth,
            final Optional<Double> mass) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        graphicalBox =
                new SegmentedBox("slab", 1, 1, 0.3, Vector3.ZERO, width / 2,
                        height / 2, depth / 2);

        final CollisionShape geometricalBox =
                new BoxShape(new Vector3f((float) width / 2,
                        (float) height / 2, (float) depth / 2));

        final Vector3f inertia = new Vector3f(0, 0, 0);
        geometricalBox.calculateLocalInertia((float) (double) mass.or(0.0),
                inertia);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo((float) (double) mass.or(0.0),
                        new DefaultMotionState(), geometricalBox, inertia);
        physicalBox = new RigidBody(info);
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
    public String toString() {
        return Objects.toStringHelper(this).add("name", name)
                .add("width", width).add("height", height).add("depth", depth)
                .toString();
    }
}
