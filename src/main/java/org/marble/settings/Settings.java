package org.marble.settings;

import java.util.prefs.Preferences;

import org.marble.RendererImpl;

/**
 * Controls the settings for the desktop version of the application.
 */
public class Settings {
    protected final Preferences prefs = Preferences
            .userNodeForPackage(Settings.class);

    public final Entry<Integer> viewportWidth = new IntegerEntry(prefs,
            "graphics/viewport/width", 800);
    public final Entry<Integer> viewportHeight = new IntegerEntry(prefs,
            "graphics/viewport/height", 600);
    public final Entry<Integer> viewportDepth = new IntegerEntry(prefs,
            "graphics/viewport/color_depth", 32);
    public final Entry<Integer> viewportDepthBufferBits = new IntegerEntry(
            prefs, "graphics/viewport/depth_buffer_bits", 8);
    public final Entry<Integer> viewportAlphaBufferBits = new IntegerEntry(
            prefs, "graphics/viewport/alpha_buffer_bits", 0);
    public final Entry<Integer> viewportStencilBufferBits = new IntegerEntry(
            prefs, "graphics/viewport/stencil_buffer_bits", 0);
    public final Entry<Integer> screenFrequency = new IntegerEntry(prefs,
            "graphics/screen/frequency", 60);
    public final Entry<Boolean> screenFullscreen = new BooleanEntry(prefs,
            "graphics/screen/fullscreen", false);
    public final Entry<Boolean> screenVerticalSync = new BooleanEntry(prefs,
            "graphics/screen/vertical_sync", false);
    public final Entry<Integer> screenSamplesPerPixel = new IntegerEntry(prefs,
            "graphics/samples_per_pixel", 0);
    public final Entry<RendererImpl> rendererImpl = new RendererEntry(prefs,
            "graphics/renderer", RendererImpl.LWJGL);
    public final Entry<Integer> framerate = new IntegerEntry(prefs,
            "graphics/framerate", -1);
    public final Entry<Boolean> stereoscopic = new BooleanEntry(prefs,
            "graphics/stereoscopic", false);
    public final Entry<Boolean> musicEnabled = new BooleanEntry(prefs,
            "audio/music/enabled", true);
    public final Entry<Boolean> soundEffectsEnabled = new BooleanEntry(prefs,
            "audio/effects/enabled", true);
}
