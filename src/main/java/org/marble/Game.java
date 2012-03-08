package org.marble;

import java.util.Set;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.marble.ball.PlayerBall;
import org.marble.block.Slab;
import org.marble.engine.Engine;
import org.marble.engine.GraphicsEngine;
import org.marble.engine.InputEngine;
import org.marble.engine.PhysicsEngine;
import org.marble.entity.Entity;
import org.marble.graphics.SmoothOrbitCamControl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.control.OrbitCamControl;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.light.PointLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.ReadOnlyTimer;

/**
 * An abstracted game instance that handles a game session.
 */
public class Game {
    // Handles rendering.
    private final GraphicsEngine graphicsEngine;
    // Handles keyboard input.
    private final InputEngine inputEngine;
    // Handles physics simulations.
    private final PhysicsEngine physicsEngine;

    // Entities that are present in our world.
    private final Set<Entity> entities = Sets.newIdentityHashSet();

    // Engines to handle.
    private final ImmutableSet<Engine<?>> engines;

    // XXX Debug light; create light entities instead.
    private PointLight light;

    // Simple tracking camera system.
    private OrbitCamControl cameraControl;

    /**
     * Creates a new game instance.
     * 
     * @param canvas
     *            The canvas to draw on.
     * @param logicalLayer
     *            The source of input events to use.
     * @param mouseManager
     *            The way to control the mouse.
     */
    public Game(final NativeCanvas canvas, final LogicalLayer logicalLayer,
            final MouseManager mouseManager) {
        graphicsEngine = new GraphicsEngine(canvas);
        inputEngine = new InputEngine(logicalLayer, mouseManager);
        physicsEngine = new PhysicsEngine();

        engines =
                ImmutableSet.of((Engine<?>) graphicsEngine,
                        (Engine<?>) inputEngine,
                        (Engine<?>) physicsEngine);
    }

    /**
     * Starts managing an entity.
     * 
     * @param entity
     *            The entity to manage.
     */
    public void addEntity(final Entity entity) {
        for (final Engine<?> engine : engines) {
            if (engine.shouldHandle(entity)) {
                engine.addEntity(entity);
            }
        }

        entities.add(entity);
    }

    /**
     * Performs deferred destruction of all subsystems.
     */
    public void destroy() {
        for (final Entity entity : entities) {
            removeEntity(entity);
        }

        // XXX Debug light
        graphicsEngine.getLighting().detach(light);

        for (final Engine<?> engine : engines) {
            engine.destroy();
        }
    }

    /**
     * The graphics engine that is in use.
     */
    public GraphicsEngine getGraphicsEngine() {
        return graphicsEngine;
    }

    /**
     * The input engine that is in use.
     */
    public InputEngine getInputEngine() {
        return inputEngine;
    }

    /**
     * The physics engine that is in use.
     */
    public PhysicsEngine getPhysicsEngine() {
        return physicsEngine;
    }

    /**
     * Performs deferred initialization of all subsystems.
     */
    public void initialize() {
        for (final Engine<?> engine : engines) {
            engine.initialize();
        }

        // XXX Debug light
        light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(100, 100, 100);
        light.setEnabled(true);
        graphicsEngine.getLighting().attach(light);

        cameraControl =
                new SmoothOrbitCamControl(graphicsEngine.getCanvas()
                        .getCanvasRenderer().getCamera(),
                        graphicsEngine.getRootNode(), 8);
        cameraControl.setSphereCoords(15, -90 * MathUtils.DEG_TO_RAD,
                30 * MathUtils.DEG_TO_RAD);

        // XXX Test entities
        final Vector3f pos = new Vector3f();

        pos.set(0, -2, 0);
        final Matrix4f slabTransform = new Matrix4f();
        slabTransform.set(pos);
        // 0 mass = immovable
        final Slab slab = new Slab("Slab", slabTransform, 32, 1, 32, 0);
        addEntity(slab);

        pos.set(0, 8, 0);
        final Matrix4f ballTransform = new Matrix4f();
        ballTransform.set(pos);
        final PlayerBall ball = new PlayerBall("Ball", ballTransform, 0.5f, 5);
        addEntity(ball);
        track(ball.getSpatial());
    }

    /**
     * Stops managing an entity.
     * 
     * @param entity
     *            The entity to stop managing.
     */
    public void removeEntity(final Entity entity) {
        for (final Engine<?> engine : engines) {
            if (engine.shouldHandle(entity)) {
                engine.removeEntity(entity);
            }
        }

        entities.remove(entity);
    }

    /**
     * Makes the camera follow a graphical spatial.
     * 
     * @param spatial
     *            The spatial to follow.
     */
    public void track(final Spatial spatial) {
        cameraControl.setLookAtSpatial(spatial);
    }

    /**
     * Advances the simulation one step.
     * 
     * @param timer
     *            The timer specifying how much time that has elapsed.
     * @return Whether the simulation should continue.
     */
    public boolean update(final ReadOnlyTimer timer) {
        boolean shouldContinue = true;
        cameraControl.update(timer.getTimePerFrame());
        for (final Engine<?> engine : engines) {
            shouldContinue &= engine.update(timer);
        }

        return shouldContinue;
    }
}
