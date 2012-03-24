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

/**
 * The main entry point for the desktop version of marble.
 *
 * This application will run the game using the LWJGL renderer and input system.
 */
public class Application implements Runnable, Scene, Updater {
    /**
     * Runs this application.
     *
     * @param args
     *            Command-line arguments specified when launching the
     *            application.
     */
    public static void main(final String[] args) {
        new Application().run();
    }

    // Is the application running?
    private boolean running = false;

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
        final RendererImpl rendererImpl = settings.rendererImpl.getValue();
        final RendererImpl.Factory rendererFactory = rendererImpl.getFactory();

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
        game = new Game(canvas, logicalLayer, mouseManager);
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
        } catch (final URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        ResourceLocatorTool.addResourceLocator(
                ResourceLocatorTool.TYPE_TEXTURE, srl);

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
            game.getGraphicsEngine().getRootNode().onDraw(renderer);
            return true;
        }
    }

    @Override
    public void run() {
        // XXX Debug; this try statement messes up the Java debugger stack
        // traces; disable it for now.
        // try {
        // Opens our window and creates associated resources.
        frameHandler.init();

        // If we've successfully gotten ourself a renderer, we're good to
        // go.
        running = true;

        // Main game loop.
        while (running) {
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
    }

    @Override
    public void update(final ReadOnlyTimer timer) {
        // Traverse the "update" update queue.
        GameTaskQueueManager
                .getManager(canvas.getCanvasRenderer().getRenderContext())
                .getQueue(GameTaskQueue.UPDATE).execute();

        // If the game wants us to quit, we quit. Once {@code running == false},
        // it can't become {@code true} again
        running &= game.update(timer);
    }
}