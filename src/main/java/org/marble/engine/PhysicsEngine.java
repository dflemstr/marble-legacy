package org.marble.engine;

import java.util.Set;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsSpace.BroadphaseType;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.marble.entity.physical.Actor;
import org.marble.entity.physical.Collidable;
import org.marble.entity.physical.Physical;
import org.marble.entity.physical.Sensor;
import org.marble.session.GameSession;
import org.marble.util.Physics;

/**
 * The JBullet-based physics engine.
 */
public class PhysicsEngine extends Engine<Physical> {
    private final Set<Actor> actors = Sets.newIdentityHashSet();
    private ImmutableMap<PhysicsCollisionObject, Physical> associations =
            ImmutableMap.of();
    private GameSession.PauseState pauseState = GameSession.PauseState.Running;
    private ImmutableSet<Physical> physicals = ImmutableSet.of();
    private PhysicsSpace physicsSpace;

    float bound = 0;

    public PhysicsEngine(final JmeContext context) {
        super(Physical.class);
    }

    public void disableDebug() {
        physicsSpace.disableDebug();
    }

    public void enableDebug(final AssetManager assetManager) {
        physicsSpace.enableDebug(assetManager);
    }

    @Override
    public void initialize() {
        physicsSpace =
                new PhysicsSpace(new Vector3f(-10000f, -10000f, -10000f),
                        new Vector3f(10000f, 10000f, 10000f),
                        BroadphaseType.DBVT);
        physicsSpace.setGravity(Physics.GRAVITY);
        physicsSpace.addCollisionListener(new PhysicsCollisionListener() {
            @Override
            public void collision(final PhysicsCollisionEvent event) {
                final Physical physicalA = associations.get(event.getObjectA());
                final Physical physicalB = associations.get(event.getObjectB());

                maybeInform(physicalA, physicalB, event);
                maybeInform(physicalB, physicalA, event);
            }

            private void
                    maybeInform(final Physical physicalA,
                            final Physical physicalB,
                            final PhysicsCollisionEvent event) {
                if (physicalA instanceof Collidable && physicalB != null) {
                    ((Collidable) physicalA).handleCollisionWith(physicalB,
                            event);
                }
            }

        });
    }

    @Override
    public void setPause(final GameSession.PauseState state) {
        pauseState = state;
    }

    @Override
    public void update(final float timePerFrame) {
        if (pauseState == GameSession.PauseState.Running) {
            for (final Physical entity : physicals) {
                if (entity.getBody().getPhysicsLocation().getZ() < bound - 64) {
                    entity.die();
                }

                entity.getBody().setPhysicsLocation(
                        entity.getTransform().getTranslation());
                entity.getBody().setPhysicsRotation(
                        entity.getTransform().getRotation());
            }
            for (final Actor actor : actors) {
                actor.performActions(timePerFrame);
            }
            physicsSpace.update(timePerFrame);
            physicsSpace.distributeEvents();
        }
    }

    @Override
    protected void entityAdded(final Physical entity) {
        physicsSpace.add(entity.getBody());
        associations =
                ImmutableMap.<PhysicsCollisionObject, Physical> builder()
                        .putAll(associations).put(entity.getBody(), entity)
                        .build();
        physicals =
                ImmutableSet.<Physical> builder().addAll(physicals).add(entity)
                        .build();
        if (entity.getTransform().getTranslation().getZ() < bound) {
            bound = entity.getTransform().getTranslation().getZ();
        }
        if (entity instanceof Actor) {
            actors.add((Actor) entity);
            if (entity instanceof Sensor) {
                for (final GhostControl sensor : ((Sensor) entity).getSensors()) {
                    physicsSpace.add(sensor);
                }
            }
        }
    }

    @Override
    protected void entityRemoved(final Physical entity) {
        physicsSpace.remove(entity.getBody());

        associations =
                ImmutableMap.copyOf(Maps.difference(associations,
                        ImmutableMap.of(entity.getBody(), entity))
                        .entriesOnlyOnLeft());
        physicals =
                ImmutableSet.copyOf(Sets.difference(physicals,
                        ImmutableSet.of(entity)));
        if (entity instanceof Actor) {
            actors.remove(entity);
            if (entity instanceof Sensor) {
                for (final GhostControl sensor : ((Sensor) entity).getSensors()) {
                    physicsSpace.remove(sensor);
                }
            }
        }
    }
}
