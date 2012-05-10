package org.marble;

import java.util.prefs.BackingStoreException;

import com.jme3.asset.AssetManager;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;
import com.jme3.system.SystemListener;
import com.jme3.system.Timer;

import com.google.common.base.Optional;

import org.marble.frp.FRPUtils;
import org.marble.frp.ReactiveListener;
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
     * Creates a new application that can be ran in different contexts.
     */
    public Application() {
        settings = new Settings();
        try {
            settings.sync();
        } catch (final BackingStoreException e) {
            e.printStackTrace();
        }

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

        appSettings.setTitle(Distribution.getProgramName());
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
     * Synchronizes some reactive {@link org.marble.settings.Settings} with some
     * {@link com.jme3.system.AppSettings}. The synchronization is persistent;
     * the values in the application settings will always reflect the values in
     * the reactive settings.
     * 
     * @param settings
     *            The reactive settings to sync from.
     * @param appSettings
     *            The application settings to sync to.
     */
    private void setupSynchronization(final Settings settings,
            final AppSettings appSettings) {
        FRPUtils.addAndCallReactiveListener(settings.viewportWidth,
                new ReactiveListener<Integer>() {

                    @Override
                    public void valueChanged(final Integer value) {
                        appSettings.setWidth(value);
                    }

                });
        FRPUtils.addAndCallReactiveListener(settings.viewportHeight,
                new ReactiveListener<Integer>() {

                    @Override
                    public void valueChanged(final Integer value) {
                        appSettings.setHeight(value);
                    }

                });
        FRPUtils.addAndCallReactiveListener(settings.viewportDepth,
                new ReactiveListener<Integer>() {

                    @Override
                    public void valueChanged(final Integer value) {
                        appSettings.setBitsPerPixel(value);
                    }

                });
        FRPUtils.addAndCallReactiveListener(settings.screenFrequency,
                new ReactiveListener<Integer>() {

                    @Override
                    public void valueChanged(final Integer value) {
                        appSettings.setFrequency(value);
                    }

                });
        FRPUtils.addAndCallReactiveListener(settings.viewportDepthBufferBits,
                new ReactiveListener<Integer>() {

                    @Override
                    public void valueChanged(final Integer value) {
                        appSettings.setDepthBits(value);
                    }

                });
        FRPUtils.addAndCallReactiveListener(settings.viewportStencilBufferBits,
                new ReactiveListener<Integer>() {

                    @Override
                    public void valueChanged(final Integer value) {
                        appSettings.setStencilBits(value);
                    }

                });
        FRPUtils.addAndCallReactiveListener(settings.screenSamplesPerPixel,
                new ReactiveListener<Integer>() {

                    @Override
                    public void valueChanged(final Integer value) {
                        appSettings.setSamples(value);
                    }

                });
        FRPUtils.addAndCallReactiveListener(settings.screenFullscreen,
                new ReactiveListener<Boolean>() {

                    @Override
                    public void valueChanged(final Boolean value) {
                        appSettings.setFullscreen(value);
                    }

                });
        FRPUtils.addAndCallReactiveListener(settings.screenVerticalSync,
                new ReactiveListener<Boolean>() {

                    @Override
                    public void valueChanged(final Boolean value) {
                        appSettings.setVSync(value);
                    }

                });
        FRPUtils.addAndCallReactiveListener(settings.framerate,
                new ReactiveListener<Integer>() {

                    @Override
                    public void valueChanged(final Integer value) {
                        appSettings.setFrameRate(value);
                    }

                });
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
        setupSynchronization(settings, context.getSettings());
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
        game.handleError(Optional.fromNullable(errorMsg), t);
        context.destroy(false);
    }

    @Override
    public void destroy() {
        game.destroy();
        context.getTimer().reset();
    }

}
