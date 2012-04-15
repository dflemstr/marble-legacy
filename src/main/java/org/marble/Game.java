package org.marble;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.ardor3d.extension.effect.bloom.BloomRenderPass;
import com.ardor3d.extension.ui.UIComponent;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
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
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.pass.RenderPass;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.extension.Skybox;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.codehaus.jparsec.error.Location;
import org.codehaus.jparsec.error.ParseErrorDetails;
import org.codehaus.jparsec.error.ParserException;

import org.marble.ball.BallKind;
import org.marble.ball.PlayerBall;
import org.marble.engine.Engine;
import org.marble.engine.GraphicsEngine;
import org.marble.engine.InputEngine;
import org.marble.engine.PhysicsEngine;
import org.marble.entity.Entity;
import org.marble.graphics.SmoothOrbitCamControl;
import org.marble.graphics.pass.DepthOfFieldPass;
import org.marble.graphics.pass.DepthPass;
import org.marble.graphics.pass.NormalPass;
import org.marble.graphics.pass.RenderVisitorPass;
import org.marble.graphics.pass.SSAOPass;
import org.marble.graphics.scene.PostIlluminationVisitor;
import org.marble.graphics.scene.PreDrawingVisitor;
import org.marble.level.LevelLoadException;
import org.marble.level.LevelLoader;
import org.marble.settings.Quality;
import org.marble.settings.Settings;
import org.marble.ui.DialogBox;
import org.marble.ui.DialogBox.Button;
import org.marble.ui.DialogBox.DialogListener;
import org.marble.ui.Menu;

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

    // Stores settings that are immediately persisted when changed
    private final Settings settings;

    // Entities that are present in our world.
    private final Set<Entity> entities = Sets.newIdentityHashSet();

    // Engines to handle.
    private final ImmutableSet<Engine<?>> engines;

    // The skybox
    private Skybox skybox;

    // Simple tracking camera system.
    private OrbitCamControl cameraControl;

    // The 2D input layer
    private UIHud hud;

    // The main menu
    private Menu menu;

    private RunState runState;

    private SSAOPass ssaoRenderPass;

    private DepthOfFieldPass depthOfFieldPass;

    private BloomRenderPass bloomRenderPass;

    private NormalPass normalPass;

    private DepthPass depthPass;
    private RenderPass renderPass;
    private RenderVisitorPass preDrawingPass;
    private RenderVisitorPass postIlluminationPass;
    long seconds = 0;

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
            final Settings settings, final DisplaySettings displaySettings) {
        this.settings = settings;
        graphicsEngine = new GraphicsEngine(canvas, displaySettings);
        inputEngine =
                new InputEngine(logicalLayer, physicalLayer, mouseManager);
        physicsEngine = new PhysicsEngine();

        engines =
                ImmutableSet.<Engine<?>> of(graphicsEngine, inputEngine,
                        physicsEngine);
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

    public RunState getRunState() {
        return runState;
    }

    public Settings getSettings() {
        return settings;
    }

    /**
     * Performs deferred initialization of all subsystems.
     */
    public void initialize() {
        for (final Engine<?> engine : engines) {
            engine.initialize();
        }

        setupSkybox();
        setupCamera();
        setupUI();
        setupPasses();

        loadLevel(Game.class.getResource("level/menu.level"));
    }

    private void loadLevel(final URL level) {

        String errorMessage = null;
        try {
            load(new LevelLoader().loadLevel(level));
        } catch (final ParserException e) {
            errorMessage =
                    describeParseError(e.getErrorDetails(), e.getLocation());

        } catch (final LevelLoadException e) {
            errorMessage =
                    e.getMessage().replace("`", "[b]").replace("'", "[/b]")
                            + "\nSee the program's error output for more information.";
            e.printStackTrace();
        } catch (final IOException e) {
            errorMessage =
                    e.getMessage().replace("`", "[b]").replace("'", "[/b]")
                            + "\nSee the program's error output for more information.";
            e.printStackTrace();
        }
        if (errorMessage != null) {
            final DialogBox dialog =
                    new DialogBox("Error while loading the level",
                            errorMessage, Optional.of(DialogBox.Icon.Critical),
                            DialogBox.Button.Abort, DialogBox.Button.Retry,
                            DialogBox.Button.Ignore);
            dialog.setDialogListener(new LevelReloadDialogListener(level));
            dialog.setLocationRelativeTo(getGraphicsEngine().getCanvas()
                    .getCanvasRenderer().getCamera());
            hud.add(dialog);
        }
    }

    private String describeParseError(final ParseErrorDetails details,
            final Location location) {
        final StringBuilder builder = new StringBuilder();

        if (location != null) {
            builder.append("The text contained an error on line [b]"
                    + location.line + "[/b], column [b]" + location.column
                    + "[/b]");
        }
        if (details != null) {
            builder.append(":\n");
            if (details.getFailureMessage() != null) {
                builder.append(details.getFailureMessage());
            } else if (!details.getExpected().isEmpty()) {
                builder.append("Expected:\n    ");
                describeParseAlternatives(builder,
                        ImmutableSet.copyOf(details.getExpected()));
                builder.append("\nbut got:\n    \"[b]");
                builder.append(details.getEncountered());
                builder.append("[/b]\"");
            } else if (details.getUnexpected() != null) {
                builder.append("Unexpected [b]")
                        .append(details.getUnexpected()).append("[/b].");
            }
        }
        return builder.toString();
    }

    private void describeParseAlternatives(final StringBuilder builder,
            final Set<String> messages) {
        if (messages.isEmpty())
            return;
        final int size = messages.size();
        int i = 0;

        builder.append("[b]");
        for (final String message : messages) {
            if (i++ > 0) {
                if (i == size) {
                    builder.append("[/b] or [b]");
                } else {
                    builder.append("[/b], [b]");
                }
            }
            builder.append(message);
        }
        builder.append("[/b]");
    }

    public void load(final ImmutableSet<Entity> level) throws ParserException,
            LevelLoadException, IOException {
        clear();
        for (final Entity entity : level) {
            addEntity(entity);
        }
        start();
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

    public void render(final Renderer renderer) {
        getGraphicsEngine().getPasses().renderPasses(renderer);
    }

    /**
     * Tells the game to restart on the next update cycle.
     */
    public void restart() {
        runState = RunState.Restarting;
    }

    /**
     * Tells the game to quit on the next update cycle.
     */
    public void quit() {
        runState = RunState.Quitting;
    }

    public void setRunState(final RunState runState) {
        this.runState = runState;
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

        hud.getLogicalLayer().checkTriggers(timer.getTimePerFrame());
        hud.updateGeometricState(timer.getTimePerFrame());
        cameraControl.update(timer.getTimePerFrame());

        final long newSeconds = (int) Math.floor(timer.getTimeInSeconds());
        if (newSeconds != seconds) {
            System.out.printf("%9.3f SPF%9.3f FPS%n", timer.getTimePerFrame(),
                    timer.getFrameRate());
            seconds = newSeconds;
        }

        for (final Engine<?> engine : engines) {
            shouldContinue &= engine.update(timer);
        }
        if (!shouldContinue) {
            runState = RunState.Quitting;
        }
    }

    private void clear() {
        for (final Entity entity : ImmutableSet.copyOf(entities)) {
            removeEntity(entity);
        }
        entities.clear();
    }

    private void setupCamera() {
        cameraControl =
                new SmoothOrbitCamControl(graphicsEngine.getCanvas()
                        .getCanvasRenderer().getCamera(),
                        graphicsEngine.getRootNode(), 8);
        cameraControl.setWorldUpVec(new Vector3(0, 0, 1));
        cameraControl.setSphereCoords(10, -90 * MathUtils.DEG_TO_RAD,
                30 * MathUtils.DEG_TO_RAD);
        cameraControl.setZoomSpeed(0.001);
        cameraControl.setupMouseTriggers(inputEngine.getLogicalLayer(), true);
    }

    private void setupPasses() {
        final BasicPassManager manager = getGraphicsEngine().getPasses();
        final Spatial rootNode = getGraphicsEngine().getRootNode();
        final DisplaySettings displaySettings =
                getGraphicsEngine().getDisplaySettings();

        {
            preDrawingPass =
                    new RenderVisitorPass(new PreDrawingVisitor(), true);
            preDrawingPass.add(rootNode);
            manager.add(preDrawingPass);
        }

        {
            renderPass = new RenderPass();
            renderPass.add(rootNode);
            manager.add(renderPass);
        }

        Texture2D depthTexture = null;
        if (settings.ssao.getValue() || settings.dof.getValue()) {
            depthPass = new DepthPass(displaySettings);
            depthPass.add(rootNode);
            depthTexture = depthPass.getDepthTexture();
            manager.add(depthPass);
        }

        Texture2D normalTexture = null;
        if (settings.ssao.getValue()) {
            normalPass = new NormalPass(displaySettings);
            normalPass.add(rootNode);
            normalTexture = normalPass.getNormalTexture();
            manager.add(normalPass);
        }

        if (settings.ssao.getValue()) {
            ssaoRenderPass =
                    new SSAOPass(displaySettings, depthTexture, normalTexture,
                            Quality.values().length
                                    - settings.ssaoQuality.getValue().ordinal());
            getInputEngine().getLogicalLayer().registerTrigger(
                    new InputTrigger(new KeyPressedCondition(Key.ONE),
                            new ToggleSSAOOnlyAO()));
            getInputEngine().getLogicalLayer().registerTrigger(
                    new InputTrigger(new KeyPressedCondition(Key.TWO),
                            new ToggleSSAOBlur()));
            manager.add(ssaoRenderPass);
        }

        {
            postIlluminationPass =
                    new RenderVisitorPass(new PostIlluminationVisitor(), true);
            postIlluminationPass.add(rootNode);
            manager.add(postIlluminationPass);
        }

        if (settings.dof.getValue()) {
            depthOfFieldPass =
                    new DepthOfFieldPass(displaySettings, depthTexture);
            depthOfFieldPass.setVignetting(settings.dofVignetting.getValue());
            depthOfFieldPass.setDepthBlur(settings.dofDepthBlur.getValue());
            depthOfFieldPass.setPentagonBokeh(settings.dofPentagonBokeh
                    .getValue());
            final int quality = settings.dofQuality.getValue().ordinal() + 1;
            depthOfFieldPass.setRings(Math.max(quality, 3));
            depthOfFieldPass.setSamples(quality);
            getInputEngine().getLogicalLayer().registerTrigger(
                    new InputTrigger(new KeyPressedCondition(Key.THREE),
                            new ToggleDOFDebugFocus()));
            manager.add(depthOfFieldPass);
        }

        if (settings.bloom.getValue()) {
            bloomRenderPass =
                    new BloomRenderPass(getGraphicsEngine().getCanvas()
                            .getCanvasRenderer().getCamera(), 1);
            bloomRenderPass.setBlurSize(0.005f);
            bloomRenderPass.setUseCurrentScene(true);
            manager.add(bloomRenderPass);
        }

        {
            final RenderPass hudPass = new RenderPass();
            hudPass.add(hud);
            manager.add(hudPass);
        }
    }

    private void setupSkybox() {
        skybox = new Skybox("skybox", 256, 256, 256);
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
        graphicsEngine.getRootNode().attachChild(skybox);
    }

    private void setupUI() {
        UIComponent.setUseTransparency(true);
        hud = new UIHud();
        hud.setupInput(getGraphicsEngine().getCanvas(), getInputEngine()
                .getPhysicalLayer(), getInputEngine().getLogicalLayer());
        hud.setMouseManager(getInputEngine().getMouseManager());

        menu = new Menu(this);
        menu.setLocationRelativeTo(getGraphicsEngine().getCanvas()
                .getCanvasRenderer().getCamera());
        hud.add(menu);

        inputEngine.getLogicalLayer().registerTrigger(
                new InputTrigger(new KeyPressedCondition(Key.ESCAPE),
                        new ToggleMenu()));
    }

    private void start() {
        final Matrix4d ballTransform = new Matrix4d();
        ballTransform.set(new Vector3d(0, 0, 8));
        final PlayerBall ball = new PlayerBall(BallKind.Wood, 0.5);
        ball.setTransform(ballTransform);
        addEntity(ball);
        track(ball.getSpatial());
    }

    private final class LevelReloadDialogListener implements DialogListener {
        private final URL level;

        private LevelReloadDialogListener(final URL level) {
            this.level = level;
        }

        @Override
        public void dialogDone(final Button pressedButton) {
            switch (pressedButton) {
            case Abort:
                quit();
                break;
            case Retry:
                loadLevel(level);
                break;
            }
            // Ignore = do nothing
        }

        @Override
        public void dialogClosed() {
            // Same as ignore
        }
    }

    /**
     * The run states that the game can be in.
     */
    public enum RunState {
        /** The game is running */
        Running,
        /** The game intends to quit */
        Quitting,
        /** The game intends to restart */
        Restarting;
    }

    private final class ToggleDOFDebugFocus implements TriggerAction {
        @Override
        public void perform(final Canvas source,
                final TwoInputStates inputStates, final double tpf) {
            depthOfFieldPass.setShowFocus(!depthOfFieldPass.doesShowFocus());
        }
    }

    /**
     * An action that toggles the visibility of the main menu.
     */
    private final class ToggleMenu implements TriggerAction {
        @Override
        public void perform(final Canvas source,
                final TwoInputStates inputStates, final double tpf) {
            menu.setVisible(!menu.isVisible());
            menu.setDirty(true);
        }
    }

    private final class ToggleSSAOBlur implements TriggerAction {
        @Override
        public void perform(final Canvas source,
                final TwoInputStates inputStates, final double tpf) {
            ssaoRenderPass.setDisableBlur(!ssaoRenderPass.shouldDisableBlur());

            final boolean reducedMode =
                    ssaoRenderPass.shouldShowOnlyAO()
                            || ssaoRenderPass.shouldDisableBlur();
            ssaoRenderPass.setIntensity(reducedMode ? 4.0f : 8.0f);
            if (depthOfFieldPass != null) {
                depthOfFieldPass.setEnabled(!reducedMode);
            }
            if (bloomRenderPass != null) {
                bloomRenderPass.setEnabled(!reducedMode);
            }
        }
    }

    private final class ToggleSSAOOnlyAO implements TriggerAction {
        @Override
        public void perform(final Canvas source,
                final TwoInputStates inputStates, final double tpf) {
            ssaoRenderPass.setShowOnlyAO(!ssaoRenderPass.shouldShowOnlyAO());

            final boolean reducedMode =
                    ssaoRenderPass.shouldShowOnlyAO()
                            || ssaoRenderPass.shouldDisableBlur();
            ssaoRenderPass.setIntensity(reducedMode ? 4.0f : 8.0f);
            if (depthOfFieldPass != null) {
                depthOfFieldPass.setEnabled(!reducedMode);
            }
            if (bloomRenderPass != null) {
                bloomRenderPass.setEnabled(!reducedMode);
            }
        }
    }
}
