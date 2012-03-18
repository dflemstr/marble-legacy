package org.marble.ball;

import java.util.Set;

import javax.vecmath.Vector3f;

import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Sphere;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;

import com.google.common.collect.ImmutableSet;

import org.marble.entity.AbstractEntity;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;
import org.marble.graphics.EntityController;
import org.marble.physics.EntityMotionState;

/**
 * A physical ball that can have different materials and physical properties.
 */
public class Ball extends AbstractEntity implements Graphical, Physical {
    protected final float radius;
    protected final float mass;
    protected Sphere graphicalSphere;
    protected RigidBody physicalSphere;
    protected BallKind kind = BallKind.Wood;

    /**
     * Creates a new ball.
     *
     * @param name
     *            The name, for debug purposes.
     * @param transform
     *            The local transform, including translation, rotation and
     *            scale.
     * @param radius
     *            The radius.
     * @param mass
     *            The base mass.
     */
    public Ball(final Float radius, final Float mass) {
        this.radius = radius;
        this.mass = mass;
    }

    @Override
    public Set<ActionInterface> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public RigidBody getBody() {
        return physicalSphere;
    }

    @Override
    public Spatial getSpatial() {
        return graphicalSphere;
    }

    @Override
    public void initialize() {
        graphicalSphere = new Sphere("ball", 16, 16, radius);
        graphicalSphere.addController(new EntityController(this));
        graphicalSphere.setRandomColors(); // XXX Debug

        final CollisionShape physicalShape = new SphereShape(radius);
        final Vector3f inertia = new Vector3f(0, 0, 0);
        physicalShape.calculateLocalInertia(mass, inertia);

        final MotionState motionState = new EntityMotionState(this);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo(mass, motionState, physicalShape,
                        inertia);
        physicalSphere = new RigidBody(info);

        setBallKind(kind);
    }

    /**
     * Changes the kind of ball that this is.
     *
     * @param kind
     *            The kind of ball to switch to.
     */
    public void setBallKind(final BallKind kind) {
        // TODO implement material properties and changes.
        switch (kind) {
        case Wood:
            break;
        case Stone:
            break;
        case Paper:
            break;
        }
        this.kind = kind;
    }
}
