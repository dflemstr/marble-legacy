package org.marble;

import com.jme3.asset.AssetManager;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;
import com.jme3.system.SystemListener;
import com.jme3.system.Timer;

import org.marble.settings.Settings;

/**
 * The main entry point for the desktop version of marble.
 * 
 * This application will run the game using the LWJGL renderer and input system.
 */
public class Application implements Runnable, SystemListener {
    // The jME application settings
    private final AppSettings appSettings;

    // Our own reactive settings system
    private final Settings settings;

    // The asset manager for loading data
    private final AssetManager assetManager;

    // The abstract game instance
    private Game game;

    // The jME context
    private JmeContext context;

    /**
     * Creates a new application that can be ran in different contexts
     */
    public Application() {
        settings = new Settings();

        // Copy relevant parts
        // TODO add reactive listeners for this
        appSettings = new AppSettings(false);
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

        appSettings.setTitle("Marble");
        appSettings.setRenderer(AppSettings.LWJGL_OPENGL2);
        appSettings.setAudioRenderer(AppSettings.LWJGL_OPENAL);
        appSettings.setUseInput(true);
        appSettings.setUseJoysticks(false);

        // Use default desktop asset manager
        assetManager =
                JmeSystem.newAssetManager(Thread.currentThread()
                        .getContextClassLoader()
                        .getResource("com/jme3/asset/Desktop.cfg"));
    }

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

    /**
     * Run this application in the given platform context.
     * 
     * @param contextType
     *            The type of context that the application is running in.
     */
    public void run(final JmeContext.Type contextType) {
        context = JmeSystem.newContext(appSettings, contextType);
        game = new Game(context, assetManager, settings);
        context.setSystemListener(this);
        context.create(false);
    }

    @Override
    public void run() {
        run(JmeContext.Type.Display);
    }

    @Override
    public void initialize() {
        game.initialize();
        context.getTimer().reset();
    }

    @Override
    public void reshape(final int width, final int height) {
        game.reshape(width, height);
    }

    @Override
    public void update() {
        final Timer timer = context.getTimer();
        timer.update();
        game.update(timer);
    }

    @Override
    public void requestClose(final boolean esc) {
        context.destroy(false);
    }

    @Override
    public void gainFocus() {
        game.resume();
    }

    @Override
    public void loseFocus() {
        game.suspend();
    }

    @Override
    public void handleError(final String errorMsg, final Throwable t) {
        game.handleError(errorMsg, t);
        System.err.println(errorMsg);
        context.destroy(false);
    }

    @Override
    public void destroy() {
        game.destroy();
        context.getTimer().reset();
    }

}
