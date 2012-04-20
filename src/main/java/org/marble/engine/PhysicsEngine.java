package org.marble.engine;

import java.util.Map;
import java.util.Set;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsSpace.BroadphaseType;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.marble.entity.physical.Actor;
import org.marble.entity.physical.Collidable;
import org.marble.entity.physical.Physical;

/**
 * The JBullet-based physics engine.
 */
public class PhysicsEngine extends Engine<Physical> {
    private PhysicsSpace physicsSpace;
    private final Map<PhysicsCollisionObject, Physical> associations = Maps
            .newIdentityHashMap();
    private final Set<Actor> actors = Sets.newIdentityHashSet();

    public PhysicsEngine(final JmeContext context) {
        super(Physical.class);
    }

    public void enableDebug(final AssetManager assetManager) {
        physicsSpace.enableDebug(assetManager);
    }

    public void disableDebug() {
        physicsSpace.disableDebug();
    }

    @Override
    public void initialize() {
        physicsSpace =
                new PhysicsSpace(new Vector3f(-10000f, -10000f, -10000f),
                        new Vector3f(10000f, 10000f, 10000f),
                        BroadphaseType.DBVT);
        physicsSpace.setGravity(new Vector3f(0, 0, -10));
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
    public void update(final float timePerFrame) {
        for (final Actor actor : actors) {
            actor.performActions(timePerFrame);
        }
        physicsSpace.update(timePerFrame);
        physicsSpace.distributeEvents();
    }

    @Override
    protected void entityAdded(final Physical entity) {
        physicsSpace.add(entity.getBody());
        associations.put(entity.getBody(), entity);
        if (entity instanceof Actor) {
            actors.add((Actor) entity);
        }
    }

    @Override
    protected void entityRemoved(final Physical entity) {
        physicsSpace.remove(entity.getBody());
        associations.remove(entity.getBody());
        if (entity instanceof Actor) {
            actors.remove(entity);
        }
    }
}
