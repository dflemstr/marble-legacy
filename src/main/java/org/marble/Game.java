package org.marble;

import java.io.IOException;
import java.util.Set;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.ardor3d.extension.ui.UIComponent;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.control.OrbitCamControl;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.extension.Skybox;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.codehaus.jparsec.error.ParserException;

import org.marble.ball.BallKind;
import org.marble.ball.PlayerBall;
import org.marble.engine.Engine;
import org.marble.engine.GraphicsEngine;
import org.marble.engine.InputEngine;
import org.marble.engine.PhysicsEngine;
import org.marble.entity.Entity;
import org.marble.graphics.PreparedDrawingVisitor;
import org.marble.graphics.SmoothOrbitCamControl;
import org.marble.level.LevelLoadException;
import org.marble.level.LevelLoader;
import org.marble.settings.Settings;

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

    private final Settings settings;

    // Entities that are present in our world.
    private final Set<Entity> entities = Sets.newIdentityHashSet();

    // Engines to handle.
    private final ImmutableSet<Engine<?>> engines;

    private Skybox skybox;

    // Simple tracking camera system.
    private OrbitCamControl cameraControl;

    Menu menu;

    boolean showMenu = true;

    public enum RunState {
        RUNNING, QUITTING, RESTARTING;
    }

    private RunState runState;

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
            final PhysicalLayer physicalLayer, final MouseManager mouseManager,
            final Settings settings) {
        this.settings = settings;
        graphicsEngine = new GraphicsEngine(canvas);
        inputEngine =
                new InputEngine(logicalLayer, physicalLayer, mouseManager);
        physicsEngine = new PhysicsEngine();

        engines =
                ImmutableSet.of((Engine<?>) graphicsEngine,
                        (Engine<?>) inputEngine, (Engine<?>) physicsEngine);
    }

    /**
     * Starts managing an entity.
     * 
     * @param entity
     *            The entity to manage.
     */
    public void addEntity(final Entity entity) {
        entity.initialize(this);
        for (final Engine<?> engine : engines)
            if (engine.shouldHandle(entity)) {
                engine.addEntity(entity);
            }

        entities.add(entity);
    }

    public void restart() {
        runState = RunState.RESTARTING;
    }

    private Skybox createSkybox() {

        skybox = new Skybox("skybox", 512, 512, 512);
        skybox.addController(new SpatialController<Skybox>() {

            @Override
            public void update(final double time, final Skybox caller) {
                caller.setTranslation(graphicsEngine.getCanvas()
                        .getCanvasRenderer().getCamera().getLocation());
            }

        });
        final Quaternion rot = Quaternion.fetchTempInstance();
        rot.fromEulerAngles(0, 0, Math.PI / 2);
        skybox.setRotation(rot);
        Quaternion.releaseTempInstance(rot);

        final String dir = "skybox/";
        final Texture north =
                TextureManager.load(dir + "north.jpg",
                        Texture.MinificationFilter.BilinearNearestMipMap, true);
        final Texture south =
                TextureManager.load(dir + "south.jpg",
                        Texture.MinificationFilter.BilinearNearestMipMap, true);
        final Texture east =
                TextureManager.load(dir + "east.jpg",
                        Texture.MinificationFilter.BilinearNearestMipMap, true);
        final Texture west =
                TextureManager.load(dir + "west.jpg",
                        Texture.MinificationFilter.BilinearNearestMipMap, true);
        final Texture up =
                TextureManager.load(dir + "up.jpg",
                        Texture.MinificationFilter.BilinearNearestMipMap, true);
        final Texture down =
                TextureManager.load(dir + "down.jpg",
                        Texture.MinificationFilter.BilinearNearestMipMap, true);

        skybox.setTexture(Skybox.Face.North, north);
        skybox.setTexture(Skybox.Face.West, west);
        skybox.setTexture(Skybox.Face.South, south);
        skybox.setTexture(Skybox.Face.East, east);
        skybox.setTexture(Skybox.Face.Up, up);
        skybox.setTexture(Skybox.Face.Down, down);
        return skybox;
    }

    /**
     * Performs deferred destruction of all subsystems.
     */
    public void destroy() {
        for (final Entity entity : entities) {
            removeEntity(entity);
        }

        graphicsEngine.getRootNode().detachChild(skybox);

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

        skybox = createSkybox();
        graphicsEngine.getRootNode().attachChild(skybox);

        UIComponent.setUseTransparency(true);

        cameraControl =
                new SmoothOrbitCamControl(graphicsEngine.getCanvas()
                        .getCanvasRenderer().getCamera(),
                        graphicsEngine.getRootNode(), 8);
        cameraControl.setWorldUpVec(new Vector3(0, 0, 1));
        cameraControl.setSphereCoords(10, -90 * MathUtils.DEG_TO_RAD,
                30 * MathUtils.DEG_TO_RAD);
        cameraControl.setZoomSpeed(0.001);
        cameraControl.setupMouseTriggers(inputEngine.getLogicalLayer(), true);

        menu = new Menu(this);

        inputEngine.getLogicalLayer().registerTrigger(
                new InputTrigger(new KeyPressedCondition(Key.ESCAPE),
                        new TriggerAction() {

                            @Override
                            public void perform(final Canvas source,
                                    final TwoInputStates inputStates,
                                    final double tpf) {
                                showMenu = !showMenu;
                            }
                        }));

        // XXX Test entities
        try {
            for (final Entity entity : new LevelLoader().loadLevel(Game.class
                    .getResource("level/core/1.level"))) {
                addEntity(entity);
            }
        } catch (final ParserException e) {
            e.printStackTrace();
        } catch (final LevelLoadException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final Matrix4d ballTransform = new Matrix4d();
        ballTransform.set(new Vector3d(0, 0, 8));
        final PlayerBall ball =
                new PlayerBall(BallKind.Wood, 0.5, Optional.of(5.0));
        ball.setTransform(ballTransform);
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
        for (final Engine<?> engine : engines)
            if (engine.shouldHandle(entity)) {
                engine.removeEntity(entity);
            }

        entities.remove(entity);
        entity.destroy();
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
     */
    public void update(final ReadOnlyTimer timer) {
        boolean shouldContinue = true;
        if (showMenu) {
            menu.update(timer);
        }
        cameraControl.update(timer.getTimePerFrame());

        for (final Engine<?> engine : engines) {
            shouldContinue &= engine.update(timer);
        }
        if (!shouldContinue) {
            runState = RunState.QUITTING;
        }
    }

    public void render(final Renderer renderer) {
        getGraphicsEngine().getRootNode().acceptVisitor(
                new PreparedDrawingVisitor(renderer), true);
        getGraphicsEngine().getRootNode().onDraw(renderer);
        renderer.renderBuckets();
        if (showMenu) {
            menu.render(renderer);
        }
    }

    public RunState getRunState() {
        return runState;
    }

    public void setRunState(final RunState runState) {
        this.runState = runState;
    }

    public Settings getSettings() {
        return settings;
    }
}
