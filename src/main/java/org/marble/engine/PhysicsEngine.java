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
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.apache.commons.lang3.tuple.Pair;

import org.marble.entity.Active;
import org.marble.entity.Collidable;
import org.marble.entity.Physical;
import org.marble.physics.CollContactAdded;
import org.marble.physics.CollContactDestroyed;
import org.marble.physics.EntityMotionState;
import org.marble.physics.Force;

/**
 * The JBullet-based physics engine.
 */
public class PhysicsEngine extends Engine<Physical> {
    private final DynamicsWorld world;
    private final Set<Physical> entities = Sets.newHashSet();
    private final Set<Pair<Physical, Physical>> contacts = Sets.newHashSet();
    private final Multimap<RigidBody, Force> forces = HashMultimap.create();

    private final Transform worldTransform = new Transform();
    private final Vector3f forceMagnitude = new Vector3f();

    public PhysicsEngine() {
        super(Physical.class);
        world = createDynamicsWorld();
        world.setGravity(new Vector3f(0, 0, -10));
    }

    @Override
    public void destroy() {
        // Do nothing
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
            for (final Force force : forces.get(body)) {
                force.calculateForce(forceMagnitude);
                body.activate();
                body.applyCentralForce(forceMagnitude);
            }
        }

        world.stepSimulation((float) timer.getTimePerFrame(), 8);
        return true;
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
    protected void entityAdded(final Physical entity) {
        final RigidBody body = entity.getBody();
        body.setMotionState(new EntityMotionState(entity));
        body.setUserPointer(entity);
        if (entity instanceof Collidable) {
            body.setCollisionFlags(CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
        }

        if (entity instanceof Active) {
            for (final Force force : ((Active) entity).getForces()) {
                forces.put(body, force);
            }
        }

        world.addRigidBody(body);
        entities.add(entity);
    }

    @Override
    protected void entityRemoved(final Physical entity) {
        final RigidBody body = entity.getBody();
        world.removeRigidBody(body);
        if (entity instanceof Active) {
            forces.removeAll(body);
        }
        entities.remove(entity);
    }
}
