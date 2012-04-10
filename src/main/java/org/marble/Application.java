package org.marble;

import java.net.URISyntaxException;

import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameHandler;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.Updater;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.Timer;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;

import org.marble.settings.Settings;

/**
 * The main entry point for the desktop version of marble.
 * 
 * This application will run the game using the LWJGL renderer and input system.
 */
public class Application implements Runnable, Scene, Updater {

    // Should we restart the application the next time we exit?
    private static boolean shouldRestart;

    /**
     * Runs this application.
     * 
     * @param args
     *            Command-line arguments specified when launching the
     *            application.
     */
    public static void main(final String[] args) {
        do {
            new Application().run();
        } while (shouldRestart);
    }

    // Measures time between updates.
    private final Timer timer;

    // The canvas that we render to.
    private final NativeCanvas canvas;

    // Schedules frame updates and renderings.
    private final FrameHandler frameHandler;

    // Handles mouse- and keyboard input.
    private final LogicalLayer logicalLayer;

    // The game that we're managing.
    private final Game game;

    /**
     * Creates a new application that can subsequently be ran using
     * {@link #run()} directly, or via a new thread.
     */
    public Application() {
        // Loads settings from the preferences system.
        final Settings settings = new Settings();

        // Convert relevant parts to display settings.
        final DisplaySettings displaySettings =
                new DisplaySettings(settings.viewportWidth.getValue(),
                        settings.viewportHeight.getValue(),
                        settings.viewportDepth.getValue(),
                        settings.screenFrequency.getValue(),
                        settings.viewportAlphaBufferBits.getValue(),
                        settings.viewportDepthBufferBits.getValue(),
                        settings.viewportStencilBufferBits.getValue(),
                        settings.screenSamplesPerPixel.getValue(),
                        settings.screenFullscreen.getValue(),
                        settings.stereoscopic.getValue());

        // We can support multiple renderers via this mechanism.
        // Currently, we only support LWJGL, but new renderers can easily be
        // added via this enum.
        final RendererFactory rendererFactory =
                settings.rendererImpl.getValue();

        // Use the factory to create all of the renderer-provided classes that
        // we need.
        canvas = rendererFactory.createCanvas(displaySettings, this);
        final PhysicalLayer physicalLayer =
                new PhysicalLayer(rendererFactory.createKeyboardWrapper(),
                        rendererFactory.createMouseWrapper(),
                        rendererFactory.createControllerWrapper(),
                        rendererFactory.createFocusWrapper(canvas));
        logicalLayer = new LogicalLayer();
        logicalLayer.registerInput(canvas, physicalLayer);
        final MouseManager mouseManager = rendererFactory.createMouseManager();
        TextureRendererFactory.INSTANCE.setProvider(rendererFactory
                .createTextureRendererProvider());

        // Set up our frame handler to schedule renders for the canvas.
        timer = new Timer();
        frameHandler = new FrameHandler(timer);
        frameHandler.addUpdater(this);
        frameHandler.addCanvas(canvas);

        // Create the game.
        game =
                new Game(canvas, logicalLayer, physicalLayer, mouseManager,
                        settings);
    }

    @Override
    public PickResults doPick(final Ray3 pickRay) {
        final PrimitivePickResults pickResults = new PrimitivePickResults();
        pickResults.setCheckDistance(true);

        // Find the objects in the scene that are hit by the ray.
        PickingUtil.findPick(game.getGraphicsEngine().getRootNode(), pickRay,
                pickResults);
        return pickResults;
    }

    @Override
    public void init() {
        // Use AWT to load images.
        // TODO use alternative image loader, and drop the dependency on AWT
        // completely.
        AWTImageLoader.registerLoader();
        SimpleResourceLocator srl;
        try {
            srl =
                    new SimpleResourceLocator(
                            ResourceLocatorTool.getClassPathResource(
                                    Application.class, "org/marble/texture/"));
            ResourceLocatorTool.addResourceLocator(
                    ResourceLocatorTool.TYPE_TEXTURE, srl);

            srl =
                    new SimpleResourceLocator(
                            ResourceLocatorTool.getClassPathResource(
                                    Application.class, "org/marble/shader/"));
            ResourceLocatorTool.addResourceLocator(
                    ResourceLocatorTool.TYPE_SHADER, srl);

            srl =
                    new SimpleResourceLocator(
                            ResourceLocatorTool.getClassPathResource(
                                    Application.class, "org/marble/model/"));
            ResourceLocatorTool.addResourceLocator(
                    ResourceLocatorTool.TYPE_MODEL, srl);
        } catch (final URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        // Perform game-specific initialization.
        game.initialize();
    }

    @Override
    public boolean renderUnto(final com.ardor3d.renderer.Renderer renderer) {
        // Traverse the "render" update queue.
        GameTaskQueueManager
                .getManager(canvas.getCanvasRenderer().getRenderContext())
                .getQueue(GameTaskQueue.RENDER).execute(renderer);

        // Clean up native resources such as old VBOs, textures etc.
        ContextGarbageCollector.doRuntimeCleanup(renderer);

        if (canvas.isClosing())
            return false;
        else {
            game.render(renderer);
            return true;
        }
    }

    @Override
    public void run() {
        // XXX Debug; this try statement messes up the Java debugger stack
        // traces; disable it for now.
        // try {

        frameHandler.init();
        // If we've successfully gotten ourself a renderer, we're good to
        // go.
        game.setRunState(Game.RunState.Running);

        // Main game loop.
        while (game.getRunState() == Game.RunState.Running) {
            frameHandler.updateFrame();
            Thread.yield();
        }
        // } finally {
        // Safely destroy the rendering system.
        final CanvasRenderer renderer = canvas.getCanvasRenderer();
        if (renderer != null) {
            renderer.makeCurrentContext();
            try {
                ContextGarbageCollector.doFinalCleanup(renderer.getRenderer());
                canvas.close();
            } finally {
                renderer.releaseCurrentContext();
            }
        }
        // }

        shouldRestart = game.getRunState() == Game.RunState.Restarting;
    }

    @Override
    public void update(final ReadOnlyTimer timer) {
        // Traverse the "update" update queue.
        GameTaskQueueManager
                .getManager(canvas.getCanvasRenderer().getRenderContext())
                .getQueue(GameTaskQueue.UPDATE).execute();

        game.update(timer);
    }
}
