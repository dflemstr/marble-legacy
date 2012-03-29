package org.marble.engine;

import javax.vecmath.Matrix4d;

import jinngine.collision.SAP2;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.DefaultScene;
import jinngine.physics.DisabledDeactivationPolicy;
import jinngine.physics.Scene;
import jinngine.physics.force.Force;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;

import com.ardor3d.math.MathUtils;
import com.ardor3d.util.ReadOnlyTimer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.marble.entity.Physical;
import org.marble.util.JinngineConversion;

/**
 * The JBullet-based physics engine.
 */
public class PhysicsEngine extends Engine<Physical> {
    private final Scene scene;
    private final BiMap<Physical, Body> bodies = HashBiMap.create();
    private final BiMap<Body, Physical> entities = bodies.inverse();

    private final Matrix3 rotation = new Matrix3();
    private final Vector3 translation = new Vector3();

    public PhysicsEngine() {
        super(Physical.class);
        scene =
                new DefaultScene(new SAP2(),
                        new NonsmoothNonlinearConjugateGradient(44),
                        new DisabledDeactivationPolicy());
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    protected void entityAdded(final Physical entity) {
        final Body body = entity.getBody();
        scene.addBody(body);
        bodies.put(entity, body);

        for (final Force force : entity.getForces()) {
            scene.addForce(force);
        }
    }

    @Override
    protected void entityRemoved(final Physical entity) {
        for (final Force force : entity.getForces()) {
            scene.removeForce(force);
        }

        final Body body = entity.getBody();
        scene.removeBody(body);
        bodies.remove(entity);
    }

    @Override
    public void initialize() {
        // Do nothing
    }

    @Override
    public boolean update(final ReadOnlyTimer timer) {
        double time =
                MathUtils.clamp(2.0D * timer.getTimePerFrame(), 0.0001, 1.0);

        int iterations = 1;
        if (time > 0.1) {
            time *= 0.1;
            iterations = 10;
        } else if (time > 0.04) {
            time *= 0.25;
            iterations = 4;
        } else if (time > 0.02) {
            time *= 0.5;
            iterations = 2;
        }

        // Update the bodies from the entity states
        for (final Physical entity : bodies.keySet()) {
            final Body body = bodies.get(entity);
            final Matrix4d transform = entity.getTransform();
            JinngineConversion.rotFromMatrix4(transform, rotation);
            JinngineConversion.transFromMatrix4(transform, translation);
            body.setPosition(translation);
            body.setOrientation(rotation);
        }

        scene.setTimestep(time);
        for (int i = 0; i < iterations; i++) {
            scene.tick();
        }

        // Update the entity states from the new body positions
        for (final Body body : entities.keySet()) {
            final Physical entity = entities.get(body);
            JinngineConversion.toMatrix4(body.getTransform(),
                    entity.getTransform());
        }

        return true;
    }
}
