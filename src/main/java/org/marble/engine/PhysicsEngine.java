package org.marble.engine;

import java.util.Set;

import javax.vecmath.Vector3f;

import com.ardor3d.util.ReadOnlyTimer;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.tuple.Pair;

import org.marble.entity.Collidable;
import org.marble.entity.Physical;
import org.marble.physics.CollContactAdded;
import org.marble.physics.CollContactDestroyed;
import org.marble.physics.EntityMotionState;

/**
 * The JBullet-based physics engine.
 */
public class PhysicsEngine extends Engine<Physical> {
    private final DynamicsWorld world;
    private final Set<Physical> entities = Sets.newHashSet();
    private final Set<Pair<Physical, Physical>> contacts = Sets.newHashSet();

    private final Transform worldTransform = new Transform();

    public PhysicsEngine() {
        super(Physical.class);
        world = createDynamicsWorld();
        world.setGravity(new Vector3f(0, 0, -10));
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
        final RigidBody body = entity.getBody();
        body.setMotionState(new EntityMotionState(entity));
        body.setUserPointer(entity);
        if (entity instanceof Collidable) {
            body.setCollisionFlags(CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
        }

        world.addRigidBody(body);
        for (final ActionInterface action : entity.getActions()) {
            world.addAction(action);
        }
        entities.add(entity);
    }

    @Override
    protected void entityRemoved(final Physical entity) {
        world.removeRigidBody(entity.getBody());
        for (final ActionInterface action : entity.getActions()) {
            world.removeAction(action);
        }
        entities.remove(entity);
    }

    @Override
    public void initialize() {
        BulletGlobals.setContactBreakingThreshold(0.1f);
        BulletGlobals.setContactAddedCallback(new CollContactAdded(contacts));
        BulletGlobals.setContactDestroyedCallback(new CollContactDestroyed(
                contacts));
    }

    @Override
    public boolean update(final ReadOnlyTimer timer) {
        for (final Physical entity : entities) {
            final RigidBody body = entity.getBody();
            body.getMotionState().getWorldTransform(worldTransform);
            body.setWorldTransform(worldTransform);
        }

        world.stepSimulation((float) timer.getTimePerFrame());
        return true;
    }
}
