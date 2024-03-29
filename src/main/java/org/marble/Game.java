package org.marble;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.jme3.asset.AssetManager;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Transform;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.LightNode;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext;
import com.jme3.system.Timer;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.ScreenController;

import org.json.JSONException;

import org.codehaus.jparsec.error.Location;
import org.codehaus.jparsec.error.ParseErrorDetails;
import org.codehaus.jparsec.error.ParserException;

import org.marble.ball.BallKind;
import org.marble.ball.PlayerBall;
import org.marble.engine.AudioEngine;
import org.marble.engine.Engine;
import org.marble.engine.GraphicsEngine;
import org.marble.engine.InputEngine;
import org.marble.engine.PhysicsEngine;
import org.marble.entity.Entity;
import org.marble.entity.EntityManager;
import org.marble.frp.FRPUtils;
import org.marble.frp.ReactiveListener;
import org.marble.frp.mutable.MutableReactive;
import org.marble.graphics.AlignedChaseCamera;
import org.marble.graphics.filter.DepthOfFieldFilter;
import org.marble.graphics.filter.SSAOFilter;
import org.marble.input.PlayerInput;
import org.marble.level.LevelLoadException;
import org.marble.level.LevelLoader;
import org.marble.level.MetaLevel;
import org.marble.level.MetaLevelPack;
import org.marble.level.StatisticalMetaLevel;
import org.marble.session.GameSession;
import org.marble.settings.Settings;
import org.marble.ui.AbstractScreenController;
import org.marble.ui.GameScreen;
import org.marble.ui.HighscoreScreen;
import org.marble.ui.LevelPackScreen;
import org.marble.ui.LevelScreen;
import org.marble.ui.LossScreen;
import org.marble.ui.PauseScreen;
import org.marble.ui.SettingsScreen;
import org.marble.ui.StartScreen;
import org.marble.ui.UIScreen;
import org.marble.ui.WinScreen;
import org.marble.util.Quality;

/**
 * An abstracted game instance that handles a game session.
 */
public class Game {
    // The kind of ball that the player should start with on each level
    public static final BallKind DEFAULT_BALL_KIND = BallKind.Wood;

    public static final float LOST_POINTS_PER_SEC = 5;

    private static Logger log = Logger.getLogger(Game.class.getName());

    // The global ambient light
    private Spatial ambientLight;
    // The platform-specific asset manager
    private final AssetManager assetManager;
    // Handles sound rendering.
    private final AudioEngine audioEngine;
    // A smoothing camera control system
    private ChaseCamera chaseCamera;

    // The jME display context
    private final JmeContext context;

    // The meta representation of the current level
    private Optional<MetaLevel> currentLevel;

    // The meta representation of the current level pack
    private MetaLevelPack currentLevelPack;

    // The URL of the current level pack
    private URL currentLevelPackURL;

    // The current game session
    private Optional<GameSession> currentSession = Optional.absent();

    // Engines to handle.
    private final ImmutableSet<Engine<?>> engines;

    // The manager of game entities
    private final EntityManager entityManager;

    // Handles rendering.
    private final GraphicsEngine graphicsEngine;

    // Handles keyboard input.
    private final InputEngine inputEngine;

    // Entities that are present in our world.
    private final LevelLoader levelLoader = new LevelLoader();

    // The HUD GUI controller
    private Nifty nifty;

    // Handles physics simulations.
    private final PhysicsEngine physicsEngine;

    // The player controlled ball, if any
    private Optional<PlayerBall> playerBall = Optional.absent();

    // Stores settings that are immediately persisted when changed
    private final Settings settings;

    // The graphical skybox
    private Spatial skybox;

    /**
     * Creates a new game instance.
     */
    public Game(final JmeContext context, final AssetManager assetManager,
            final Settings settings) {
        this.settings = settings;
        this.context = context;
        this.assetManager = assetManager;

        graphicsEngine = new GraphicsEngine(context);
        inputEngine = new InputEngine(context);
        physicsEngine = new PhysicsEngine(context);
        audioEngine = new AudioEngine(context);

        engines =
                ImmutableSet.<Engine<?>> of(graphicsEngine, inputEngine,
                        physicsEngine, audioEngine);
        entityManager = new EntityManager(this);
    }

    /**
     * Performs deferred destruction of all subsystems.
     */
    public void destroy() {
        entityManager.removeAllEntities();

        for (final Engine<?> engine : engines) {
            engine.destroy();
        }
    }

    /**
     * The current game asset manager.
     */
    public AssetManager getAssetManager() {
        return assetManager;
    }

    /**
     * The currently played level.
     */
    public Optional<MetaLevel> getCurrentLevel() {
        return currentLevel;
    }

    /**
     * The currently loaded level pack.
     */
    public MetaLevelPack getCurrentLevelPack() {
        return currentLevelPack;
    }

    /**
     * The URL for the currently loaded level.
     */
    public URL getCurrentLevelPackURL() {
        return currentLevelPackURL;
    }

    /**
     * The current game session, if any.
     */
    public Optional<GameSession> getCurrentSession() {
        return currentSession;
    }

    /**
     * The set of running engines.
     */
    public ImmutableSet<Engine<?>> getEngines() {
        return engines;
    }

    /**
     * The current entity manager, containing loaded entities.
     */
    public EntityManager getEntityManager() {
        return entityManager;
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
     * The reactive game settings.
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Go to the menu screen.
     */
    public void gotoMenu() {
        load(new MetaLevel("Menu level",
                Game.class.getResource("level/menu.level"),
                Optional.<URL> absent(),
                UUID.fromString("a6f6b07a-5a95-4328-a73f-c848f4c52788")));
        trackSpatial(getGraphicsEngine().getRootNode());
        playerBall = Optional.absent();
        currentSession = Optional.absent();
        setPause(GameSession.PauseState.Running);
        gotoScreen(UIScreen.Start);
    }

    /**
     * Go to the specified UI screen.
     * 
     * @param screen
     *            The screen to go to.
     */
    public void gotoScreen(final UIScreen screen) {
        nifty.gotoScreen(screen.getName());
    }

    /**
     * Handles an exception in the program.
     * 
     * @param errorMessage
     *            A description of the error.
     * @param t
     *            The machine representation of the exception.
     */
    public void handleError(final Optional<String> errorMessage,
            final Throwable t) {
        logStackTrace(t);
        final String message;
        if (errorMessage.isPresent()) {
            message = errorMessage.get();
        } else {
            message = t.getLocalizedMessage();
        }
        JOptionPane.showMessageDialog(null, message, "An error has occurred",
                JOptionPane.ERROR_MESSAGE);
        gotoMenu();
    }

    /**
     * Handles an exception in the program.
     * 
     * @param errorMessage
     *            A description of the error.
     * @param t
     *            The machine representation of the exception.
     */
    public void handleError(final String errorMessage, final Throwable t) {
        handleError(Optional.of(errorMessage), t);
    }

    /**
     * Handles an exception in the program.
     * 
     * @param t
     *            The machine representation of the exception.
     */
    public void handleError(final Throwable t) {
        handleError(Optional.<String> absent(), t);
    }

    /**
     * Checks whether the current level pack has a next level.
     */
    public boolean hasNextLevel() {
        return currentLevel.isPresent()
                && currentLevelPack.getLevels().indexOf(currentLevel.get()) + 1 < currentLevelPack
                        .getLevels().size();
    }

    /**
     * Performs deferred initialization of all subsystems.
     */
    public void initialize() {
        Logger.getLogger("com.jme3").setLevel(Level.WARNING);
        Logger.getLogger("").setLevel(Level.WARNING);

        for (final Engine<?> engine : engines) {
            engine.initialize();
        }

        setupGUI();
        setupControls();
        setupSkybox();
        setupLighting();
        setupCamera();
        setupFilters();

        loadLevelPack(Game.class.getResource("level/core.pack"));

        gotoMenu();
    }

    /**
     * Kills the ball, either respawning it or making the player lose depending
     * on available lives.
     */
    public void killBall() {
        if (currentSession.isPresent()) {
            final GameSession session = currentSession.get();
            final int lives = session.getLives();
            if (lives > 0) {
                session.setLives(lives - 1);
                respawnBall();
            } else {
                loseLevel();
            }
        }
    }

    /**
     * Tries to replace the currently loaded level pack with the level pack at
     * the specified URL.
     * 
     * @param levelPack
     *            The URL to the level pack to try to load.
     */
    public void loadLevelPack(final URL levelPack) {
        try {
            currentLevelPackURL = levelPack;
            currentLevelPack = levelLoader.loadMetaLevelPack(levelPack);
            return;
        } catch (final IOException e) {
            handleError(e);
        } catch (final JSONException e) {
            handleError(e);
        } catch (final LevelLoadException e) {
            handleError(e);
        }
    }

    /**
     * Loads the next level, if there is one.
     */
    public void loadNextLevel() {
        if (currentLevel.isPresent()) {
            final int nextLevelIndex =
                    currentLevelPack.getLevels().indexOf(currentLevel.get()) + 1;
            if (nextLevelIndex < currentLevelPack.getLevels().size()) {
                playLevel(currentLevelPack.getLevels().get(nextLevelIndex));
            } else
                throw new RuntimeException("There is no next level");
        }
    }

    /**
     * Makes the player lose the current level.
     */
    public void loseLevel() {
        setPause(GameSession.PauseState.EnforcedPause);
        gotoScreen(UIScreen.Loss);
    }

    /**
     * Starts a game on the specified level.
     * 
     * @param level
     *            The level to load
     */
    public void playLevel(final MetaLevel level) {
        load(level);
        start();
        gotoScreen(UIScreen.Game);
    }

    /**
     * Tells the game to quit on the next update cycle.
     */
    public void quit() {
        context.destroy(false);
    }

    /**
     * Reshapes the viewport of the game.
     * 
     * @param width
     *            The new viewport width.
     * @param height
     *            The new viewport height.
     */
    public void reshape(final int width, final int height) {
        graphicsEngine.reshape(width, height);
        if (nifty != null) {
            nifty.resolutionChanged();
        }
    }

    /**
     * Respawns the ball at the last respawn point.
     */
    public void respawnBall() {
        if (playerBall.isPresent() && currentSession.isPresent()) {
            final PlayerBall ball = playerBall.get();
            try {
                ball.setBallKind(currentSession.get().getRespawnKind(), true,
                        true);
            } catch (final Exception e) {
                handleError(e);
            }
            ball.resetMoveTo(currentSession.get().getRespawnPoint());
        }
    }

    /**
     * Tells the game to restart the context (Graphics etc.) on the next update
     * cycle.
     */
    public void restartContext() {
        context.restart();
    }

    /**
     * Resumes the game from having been suspended. Do not confuse with
     * unpausing the game.
     */
    public void resume() {
        for (final Engine<?> engine : engines) {
            engine.resume();
        }
    }

    /**
     * Switches to a new level pack.
     */
    public void setCurrentLevelPack(final MetaLevelPack currentLevelPack) {
        this.currentLevelPack = currentLevelPack;
    }

    /**
     * Sets the current pause state and updates the GUI screens accordingly.
     */
    public void setPause(final GameSession.PauseState state) {
        for (final Engine<?> engine : engines) {
            engine.setPause(state);
        }
        if (currentSession.isPresent()) {
            currentSession.get().setPauseState(state);
            if (state == GameSession.PauseState.PlayerPaused) {
                gotoScreen(UIScreen.Pause);
            } else if (state == GameSession.PauseState.Running) {
                gotoScreen(UIScreen.Game);
            }
        }
    }

    /**
     * Shows the highscore screen for the specified level.
     */
    public void showHighscores(final MetaLevel level) {
        gotoScreen(UIScreen.Highscores);
    }

    /**
     * Tells the game to halt immediately.
     */
    public void stop() {
        context.destroy(false);
    }

    /**
     * Suspends the game. This happens when the window loses focus, or when our
     * android activity is suspended.
     */
    public void suspend() {
        for (final Engine<?> engine : engines) {
            engine.suspend();
        }
    }

    /**
     * Pauses/unpauses the game with a player-paused state
     */
    public void togglePause() {
        if (currentSession.isPresent()) {
            final GameSession session = currentSession.get();
            if (session.getPauseState() == GameSession.PauseState.Running) {
                setPause(GameSession.PauseState.PlayerPaused);
            } else if (session.getPauseState() == GameSession.PauseState.PlayerPaused) {
                setPause(GameSession.PauseState.Running);
            }
        }
    }

    /**
     * Makes the camera follow a graphical spatial.
     * 
     * @param spatial
     *            The spatial to follow.
     */
    public void trackSpatial(final Spatial spatial) {
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
        for (final Engine<?> engine : engines) {
            engine.update(timer.getTimePerFrame());
        }
        if (!currentSession.isPresent()
                || currentSession.get().getPauseState() == GameSession.PauseState.Running) {
            if (currentSession.isPresent()
                    && currentSession.get().getPoints() > 0) {
                final GameSession session = currentSession.get();
                session.setPoints(session.getPoints() - LOST_POINTS_PER_SEC
                        * timer.getTimePerFrame());
            }

            entityManager.update(timer.getTimePerFrame());
        }
    }

    /**
     * Makes the player win the current level.
     */
    public void winLevel() {
        setPause(GameSession.PauseState.EnforcedPause);

        final MutableReactive<StatisticalMetaLevel> entry =
                settings.levelStatistics.getEntry(currentLevel.get().getUUID());
        final StatisticalMetaLevel stats = entry.getValue();

        final String playerName = settings.playerName.getValue();
        final Integer score = (int) currentSession.get().getPoints();

        if (!stats.getHighscores().containsKey(playerName)
                || stats.getHighscores().get(playerName) < score) {
            final ImmutableMap.Builder<String, Integer> highscoreBuilder =
                    ImmutableMap.builder();
            highscoreBuilder.put(playerName, (int) currentSession.get()
                    .getPoints());
            highscoreBuilder.putAll(Maps.filterKeys(stats.getHighscores(),
                    Predicates.not(Predicates.equalTo(playerName))));

            final StatisticalMetaLevel newStats =
                    new StatisticalMetaLevel(highscoreBuilder.build());

            entry.setValue(newStats);
            System.out.println("Saved: " + newStats.getHighscores());
        }

        gotoScreen(UIScreen.Win);
    }

    /**
     * Describes various alternatives that a parser is choosing between.
     * 
     * @param builder
     *            The string builder to append to.
     * @param messages
     *            The set of alternatives.
     */
    private void describeParseAlternatives(final StringBuilder builder,
            final Set<String> messages) {
        if (messages.isEmpty())
            return;

        final int size = messages.size();
        int i = 0;

        for (final String message : messages) {
            if (i > 0) {
                if (i == size) {
                    builder.append(" or ");
                } else {
                    builder.append(", ");
                }
            }
            i++;
            builder.append(message);
        }
    }

    /**
     * Describes a parse error in a level file.
     * 
     * @param details
     *            The parser-provided error details.
     * @param location
     *            The location of the syntax error.
     * @return A string describing the error.
     */
    private String describeParseError(final ParseErrorDetails details,
            final Location location) {
        final StringBuilder builder = new StringBuilder();

        if (location != null) {
            builder.append("The text contained an error on line "
                    + location.line + ", column " + location.column);
        }
        if (details != null) {
            builder.append(":\n");
            if (details.getFailureMessage() != null) {
                builder.append(details.getFailureMessage());
            } else if (!details.getExpected().isEmpty()) {
                builder.append("Expected:\n    ");
                describeParseAlternatives(builder,
                        ImmutableSet.copyOf(details.getExpected()));
                builder.append("\nbut got:\n    \"");
                builder.append(details.getEncountered());
                builder.append("\"");
            } else if (details.getUnexpected() != null) {
                builder.append("Unexpected ").append(details.getUnexpected())
                        .append(".");
            }
        }
        return builder.toString();
    }

    /**
     * Loads the specified set of entities and starts the simulation of a level.
     * 
     * @param level
     *            The entities to start the simulation of.
     */
    private void load(final ImmutableSet<Entity> level) {
        entityManager.removeAllEntities();
        entityManager.addEntities(level);
    }

    /**
     * Only loads and starts the specified level; doesn't update the GUI.
     * 
     * @param level
     *            The level to load
     */
    private void load(final MetaLevel level) {
        try {
            load(levelLoader.loadLevel(level.getUri()));
            currentLevel = Optional.of(level);
        } catch (final ParserException e) {
            handleError(
                    describeParseError(e.getErrorDetails(), e.getLocation()), e);
        } catch (final LevelLoadException e) {
            handleError(e);
        } catch (final Exception e) {
            handleError(e);
        }
    }

    /**
     * Prints a stack trace to the log.
     * 
     * @param t
     *            The throwable whose stack trace should be logged.
     */
    private void logStackTrace(final Throwable t) {
        log.severe(t.toString());
        final StackTraceElement[] trace = t.getStackTrace();
        for (final StackTraceElement traceElem : trace) {
            log.severe("\tat " + traceElem);
        }

        final Throwable cause = t.getCause();
        if (cause != null) {
            logStackTrace(cause);
        }
    }

    /**
     * Initialize the smoothing camera system.
     */
    private void setupCamera() {
        chaseCamera = new AlignedChaseCamera(graphicsEngine.getCamera());
        chaseCamera.setDefaultHorizontalRotation(FastMath.HALF_PI);
        chaseCamera.setDefaultDistance(10);
        chaseCamera.setChasingSensitivity(20);
        chaseCamera.setSmoothMotion(true);
        chaseCamera.setTrailingEnabled(false);
    }

    /**
     * Initialize global keyboard controls.
     */
    private void setupControls() {
        final InputManager inputManager = getInputEngine().getInputManager();

        // TODO make configurable
        inputManager.addMapping(PlayerInput.MoveForward.getName(),
                new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping(PlayerInput.MoveBackward.getName(),
                new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping(PlayerInput.MoveLeft.getName(), new KeyTrigger(
                KeyInput.KEY_LEFT));
        inputManager.addMapping(PlayerInput.MoveRight.getName(),
                new KeyTrigger(KeyInput.KEY_RIGHT));

        inputManager.addMapping(PlayerInput.Pause.getName(), new KeyTrigger(
                KeyInput.KEY_ESCAPE));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(final String name, final boolean isPressed,
                    final float tpf) {
                if (!isPressed) {
                    togglePause();
                }
            }
        }, PlayerInput.Pause.getName());
    }

    /**
     * Initialize reactive post-processing filters.
     */
    private void setupFilters() {
        final ViewPort viewPort = getGraphicsEngine().getViewPort();
        final FilterPostProcessor filters =
                new FilterPostProcessor(assetManager);
        {
            final SSAOFilter ssaoFilter = new SSAOFilter();
            ssaoFilter.setIntensity(6.0f);
            FRPUtils.addAndCallReactiveListener(settings.ssao,
                    new ReactiveListener<Boolean>() {
                        @Override
                        public void valueChanged(final Boolean value) {
                            ssaoFilter.setEnabled(value);
                        }
                    });
            filters.addFilter(ssaoFilter);
        }
        {
            final DepthOfFieldFilter advDofFilter = new DepthOfFieldFilter();
            advDofFilter.setRings(3);
            advDofFilter.setGain(8);
            advDofFilter.setFocalDepth(3.0f);
            advDofFilter.setDepthBlur(true);
            advDofFilter.setDepthBlurSize(1.0f);
            advDofFilter.setThreshold(0.9f);
            FRPUtils.addAndCallReactiveListener(settings.dof,
                    new ReactiveListener<Boolean>() {
                        @Override
                        public void valueChanged(final Boolean value) {
                            advDofFilter.setEnabled(value);
                        }
                    });
            FRPUtils.addAndCallReactiveListener(settings.dofQuality,
                    new ReactiveListener<Quality>() {
                        @Override
                        public void valueChanged(final Quality value) {
                            advDofFilter.setSamples(value.getIndex() + 1);
                        }
                    });

            FRPUtils.addAndCallReactiveListener(settings.dofVignetting,
                    new ReactiveListener<Boolean>() {
                        @Override
                        public void valueChanged(final Boolean value) {
                            advDofFilter.setVignetting(value);
                        }
                    });

            FRPUtils.addAndCallReactiveListener(settings.dofDepthBlur,
                    new ReactiveListener<Boolean>() {
                        @Override
                        public void valueChanged(final Boolean value) {
                            advDofFilter.setDepthBlur(value);
                        }
                    });

            FRPUtils.addAndCallReactiveListener(settings.dofPentagonBokeh,
                    new ReactiveListener<Boolean>() {
                        @Override
                        public void valueChanged(final Boolean value) {
                            advDofFilter.setPentagonBokeh(value);
                        }
                    });

            filters.addFilter(advDofFilter);
        }
        {
            final BloomFilter bloomFilter = new BloomFilter();
            FRPUtils.addAndCallReactiveListener(settings.bloom,
                    new ReactiveListener<Boolean>() {
                        @Override
                        public void valueChanged(final Boolean value) {
                            bloomFilter.setEnabled(value);
                        }
                    });

            bloomFilter.setDownSamplingFactor(2);
            bloomFilter.setBlurScale(1.37f);
            bloomFilter.setExposurePower(2.0f);
            bloomFilter.setBloomIntensity(0.5f);
            filters.addFilter(bloomFilter);
        }
        viewPort.addProcessor(filters);
    }

    /**
     * Initialize GUI subsystem
     */
    private void setupGUI() {
        final NiftyJmeDisplay niftyDisplay =
                new NiftyJmeDisplay(assetManager,
                        inputEngine.getInputManager(),
                        audioEngine.getAudioRenderer(),
                        graphicsEngine.getGuiViewPort());
        nifty = niftyDisplay.getNifty();

        final Properties globalProperties = new Properties();
        globalProperties.setProperty("program.name",
                Distribution.getProgramName());
        globalProperties.setProperty("program.version",
                Distribution.getVersion());
        globalProperties.setProperty("program.copyright",
                Distribution.getCopyright());
        globalProperties.setProperty("program.description",
                Distribution.getProgramDescription());
        nifty.setGlobalProperties(globalProperties);

        // XXX Determine whether this association still can be useful, or
        // replace with list/set/ArrayList/nothing
        final ImmutableMap.Builder<UIScreen, AbstractScreenController> screenControllerBuilder =
                ImmutableMap.builder();

        screenControllerBuilder.put(UIScreen.Start, new StartScreen(this));
        screenControllerBuilder.put(UIScreen.Game, new GameScreen(this));
        screenControllerBuilder
                .put(UIScreen.Settings, new SettingsScreen(this));
        screenControllerBuilder.put(UIScreen.Levels, new LevelScreen(this));
        screenControllerBuilder.put(UIScreen.LevelPacks, new LevelPackScreen(
                this));
        screenControllerBuilder.put(UIScreen.Pause, new PauseScreen(this));
        screenControllerBuilder.put(UIScreen.Win, new WinScreen(this));
        screenControllerBuilder.put(UIScreen.Loss, new LossScreen(this));
        screenControllerBuilder.put(UIScreen.Highscores, new HighscoreScreen(
                this));

        final ImmutableMap<UIScreen, AbstractScreenController> screenControllers =
                screenControllerBuilder.build();

        nifty.fromXml("Interface/Nifty/Marble.xml", "start", screenControllers
                .values().toArray(new ScreenController[0]));

        graphicsEngine.getGuiViewPort().addProcessor(niftyDisplay);
    }

    /**
     * Initialize global lighting.
     */
    private void setupLighting() {
        final AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        ambientLight = new LightNode("ambient", ambient);
        getGraphicsEngine().getRootNode().attachChild(ambientLight);
        getGraphicsEngine().getRootNode().addLight(ambient);
    }

    /**
     * Initialize the graphical skybox.
     */
    private void setupSkybox() {
        final Texture texture =
                assetManager
                        .loadTexture("Textures/Sky/Stormydays/Stormydays.dds");
        skybox = SkyFactory.createSky(assetManager, texture, false);
        skybox.rotate(FastMath.HALF_PI, 0, 0);
        graphicsEngine.getRootNode().attachChild(skybox);
    }

    /**
     * Start the game simulation by introducing a player ball, a new game
     * session, and unpausing the game.
     */
    private void start() {
        final Transform ballTransform = new Transform();
        ballTransform.setTranslation(0, 0, 2);

        final PlayerBall ball = new PlayerBall(DEFAULT_BALL_KIND);
        ball.setTransform(ballTransform);

        entityManager.addEntities(ImmutableSet.<Entity> of(ball));
        trackSpatial(ball.getSpatial());

        playerBall = Optional.of(ball);
        currentSession = Optional.of(new GameSession());

        setPause(GameSession.PauseState.Running);
    }
}
