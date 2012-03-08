package org.marble.engine;

import javax.vecmath.Vector3f;

import org.marble.entity.Physical;

import com.ardor3d.util.ReadOnlyTimer;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

/**
 * The JBullet-based physics engine.
 */
public class PhysicsEngine extends Engine<Physical> {
    private final DynamicsWorld world;

    public PhysicsEngine() {
        super(Physical.class);
        world = createDynamicsWorld();
        world.setGravity(new Vector3f(0, -10, 0));
    }

    private DynamicsWorld createDynamicsWorld() {
        final DefaultCollisionConfiguration collisionConfiguration =
                new DefaultCollisionConfiguration();
        final CollisionDispatcher dispatcher =
                new CollisionDispatcher(collisionConfiguration);

        // Set up a broadphase traverser with the given world bounds.
        final BroadphaseInterface broadphase =
                new AxisSweep3(new Vector3f(-1024, -1024, -1024), new Vector3f(
                        1024, 1024, 1024), 1024);

        final ConstraintSolver solver = new SequentialImpulseConstraintSolver();

        return new DiscreteDynamicsWorld(dispatcher, broadphase, solver,
                collisionConfiguration);
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    protected void entityAdded(final Physical entity) {
        world.addRigidBody(entity.getBody());
        for (final ActionInterface action : entity.getActions()) {
            world.addAction(action);
        }
    }

    @Override
    protected void entityRemoved(final Physical entity) {
        world.removeRigidBody(entity.getBody());
        for (final ActionInterface action : entity.getActions()) {
            world.removeAction(action);
        }
    }

    @Override
    public void initialize() {
        // Do nothing
    }

    @Override
    public boolean update(final ReadOnlyTimer timer) {
        world.stepSimulation((float) timer.getTimePerFrame());
        return true;
    }
}
