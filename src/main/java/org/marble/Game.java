package org.marble;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import com.jme3.asset.AssetManager;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Transform;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.LightNode;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.Timer;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

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
import org.marble.graphics.AlignedChaseCamera;
import org.marble.graphics.filter.DepthOfFieldFilter;
import org.marble.graphics.filter.SSAOFilter;
import org.marble.input.PlayerInput;
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

    // Stores settings that are immediately persisted when changed
    private final Settings settings;

    // Entities that are present in our world.
    private final Set<Entity> entities = Sets.newIdentityHashSet();

    // Engines to handle.
    private final ImmutableSet<Engine<?>> engines;

    private final JmeContext context;
    private final AssetManager assetManager;

    private ChaseCamera chaseCamera;
    private Spatial skybox;
    private Spatial ambientLight;

    /**
     * Creates a new game instance.
     */
    public Game(final JmeContext context, final AssetManager assetManager,
            final Settings settings) {
        this.settings = settings;
        graphicsEngine = new GraphicsEngine(context);
        inputEngine = new InputEngine(context);
        physicsEngine = new PhysicsEngine(context);

        engines =
                ImmutableSet.<Engine<?>> of(graphicsEngine, inputEngine,
                        physicsEngine);

        this.context = context;
        this.assetManager = assetManager;
    }

    /**
     * Starts managing an entity.
     * 
     * @param entity
     *            The entity to manage.
     */
    public void addEntity(final Entity entity) {
        entity.initialize(this);
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
        for (final Entity entity : ImmutableSet.copyOf(entities)) {
            removeEntity(entity);
        }

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

        setupControls();
        setupSkybox();
        setupLighting();
        setupCamera();
        setupUI();
        setupFilters();

        loadLevel(Game.class.getResource("level/menu.level"));
    }

    private void setupControls() {
        final InputManager inputManager = getInputEngine().getInputManager();
        inputManager.addMapping(PlayerInput.MoveForward.getName(),
                new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping(PlayerInput.MoveBackward.getName(),
                new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping(PlayerInput.MoveLeft.getName(), new KeyTrigger(
                KeyInput.KEY_LEFT));
        inputManager.addMapping(PlayerInput.MoveRight.getName(),
                new KeyTrigger(KeyInput.KEY_RIGHT));
    }

    private void setupLighting() {
        final AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f));
        ambientLight = new LightNode("ambient", ambient);
        getGraphicsEngine().getRootNode().attachChild(ambientLight);
        getGraphicsEngine().getRootNode().addLight(ambient);
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
        // TODO reintroduce error handling
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

    /**
     * Tells the game to halt immediately.
     */
    public void stop() {
        context.destroy(false);
    }

    /**
     * Tells the game to restart the context (Graphics etc.) on the next update
     * cycle.
     */
    public void restartContext() {
        final AppSettings appSettings = context.getSettings();
        appSettings.setWidth(settings.viewportWidth.getValue());
        appSettings.setHeight(settings.viewportHeight.getValue());
        appSettings.setBitsPerPixel(settings.viewportDepth.getValue());
        appSettings.setFrequency(settings.screenFrequency.getValue());
        appSettings.setDepthBits(settings.viewportDepthBufferBits.getValue());
        appSettings.setStencilBits(settings.viewportStencilBufferBits
                .getValue());
        appSettings.setSamples(settings.screenSamplesPerPixel.getValue());
        appSettings.setFullscreen(settings.screenFullscreen.getValue());
        appSettings.setVSync(settings.screenVerticalSync.getValue());
        appSettings.setFrameRate(settings.framerate.getValue());

        context.restart();
    }

    /**
     * Tells the game to quit on the next update cycle.
     */
    public void quit() {
        context.destroy(false);
    }

    /**
     * Makes the camera follow a graphical spatial.
     * 
     * @param spatial
     *            The spatial to follow.
     */
    public void track(final Spatial spatial) {
        chaseCamera.setSpatial(spatial);
    }

    /**
     * Advances the simulation one step.
     * 
     * @param timer
     *            The timer specifying how much time that has elapsed.
     */
    public void update(final Timer timer) {
        chaseCamera.update(timer.getTimePerFrame());
        System.out.println(timer.getFrameRate());
        for (final Engine<?> engine : engines) {
            engine.update(timer.getTimePerFrame());
        }
    }

    private void clear() {
        for (final Entity entity : ImmutableSet.copyOf(entities)) {
            removeEntity(entity);
        }
        entities.clear();
    }

    private void setupCamera() {
        chaseCamera =
                new AlignedChaseCamera(graphicsEngine.getCamera(),
                        inputEngine.getInputManager());
        chaseCamera.setDefaultHorizontalRotation(FastMath.HALF_PI);
        chaseCamera.setDefaultDistance(10);
        chaseCamera.setChasingSensitivity(20);
        chaseCamera.setSmoothMotion(true);
        chaseCamera.setTrailingEnabled(false);
    }

    private void setupFilters() {
        final ViewPort viewPort = getGraphicsEngine().getViewPort();
        final FilterPostProcessor filters =
                new FilterPostProcessor(assetManager);
        if (settings.ssao.getValue()) {
            final SSAOFilter ssaoFilter = new SSAOFilter();
            filters.addFilter(ssaoFilter);
        }
        if (settings.dof.getValue()) {
            final DepthOfFieldFilter advDofFilter = new DepthOfFieldFilter();
            final int quality = settings.dofQuality.getValue().ordinal() + 1;
            advDofFilter.setVignetting(settings.dofVignetting.getValue());
            advDofFilter.setDepthBlur(settings.dofDepthBlur.getValue());
            advDofFilter.setPentagonBokeh(settings.dofPentagonBokeh.getValue());
            advDofFilter.setRings(3);
            advDofFilter.setSamples(quality);
            filters.addFilter(advDofFilter);
        }
        if (settings.bloom.getValue()) {
            final BloomFilter bloomFilter = new BloomFilter();
            bloomFilter.setDownSamplingFactor(2);
            bloomFilter.setBlurScale(1.37f);
            bloomFilter.setExposurePower(3.30f);
            bloomFilter.setBloomIntensity(2.45f);
            filters.addFilter(bloomFilter);
        }
        viewPort.addProcessor(filters);

    }

    private void setupSkybox() {
        final String dir = "Textures/Sky/Lagoon/";
        final Texture north = assetManager.loadTexture(dir + "north.jpg");
        final Texture south = assetManager.loadTexture(dir + "south.jpg");
        final Texture east = assetManager.loadTexture(dir + "east.jpg");
        final Texture west = assetManager.loadTexture(dir + "west.jpg");
        final Texture up = assetManager.loadTexture(dir + "up.jpg");
        final Texture down = assetManager.loadTexture(dir + "down.jpg");

        skybox =
                SkyFactory.createSky(assetManager, west, east, north, south,
                        down, up);
        skybox.rotate(FastMath.HALF_PI, 0, 0);
        graphicsEngine.getRootNode().attachChild(skybox);
    }

    private void setupUI() {
        /*
         * UIComponent.setUseTransparency(true); hud = new UIHud();
         * hud.setupInput(getGraphicsEngine().getCanvas(), getInputEngine()
         * .getPhysicalLayer(), getInputEngine().getLogicalLayer());
         * hud.setMouseManager(getInputEngine().getMouseManager());
         * 
         * menu = new Menu(this);
         * menu.setLocationRelativeTo(getGraphicsEngine().getCanvas()
         * .getCanvasRenderer().getCamera()); hud.add(menu);
         * 
         * inputEngine.getLogicalLayer().registerTrigger( new InputTrigger(new
         * KeyPressedCondition(Key.ESCAPE), new ToggleMenu()));
         */
    }

    private void start() {
        final Transform ballTransform = new Transform();
        ballTransform.setTranslation(0, 0, 8);
        final PlayerBall ball = new PlayerBall(BallKind.Glass);
        ball.setTransform(ballTransform);
        addEntity(ball);
        track(ball.getSpatial());

        // getPhysicsEngine().enableDebug(assetManager);
    }

    public void handleError(final String errorMessage, final Throwable t) {
        if (errorMessage != null) {
            /*
             * final DialogBox dialog = new
             * DialogBox("Error while loading the level", errorMessage,
             * Optional.of(DialogBox.Icon.Critical), DialogBox.Button.Abort,
             * DialogBox.Button.Retry, DialogBox.Button.Ignore);
             * dialog.setDialogListener(new LevelReloadDialogListener(level));
             * dialog.setLocationRelativeTo(getGraphicsEngine().getCanvas()
             * .getCanvasRenderer().getCamera()); hud.add(dialog);
             */
        }
        t.printStackTrace();
    }

    public void resume() {
        for (final Engine<?> engine : engines) {
            engine.resume();
        }
    }

    public void suspend() {
        for (final Engine<?> engine : engines) {
            engine.suspend();
        }
    }

    public void reshape(final int width, final int height) {
        graphicsEngine.reshape(width, height);
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }
}
