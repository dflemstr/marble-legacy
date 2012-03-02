package org.marble;

import java.util.prefs.Preferences;

/**
 * Controls the settings for the desktop version of the application.
 */
public class Settings {
    protected final Preferences prefs = Preferences
            .userNodeForPackage(Settings.class);

    /**
     * An entry in the settings registry.
     * 
     * @param <A>
     *            The type of the setting being stored in this entry.
     */
    public abstract class Entry<A> {
        protected final String node;
        protected final A defaultValue;

        /**
         * Constructs a new settings entry.
         * 
         * @param node
         *            The settings node to store this setting in. Can be a
         *            slash-separated path in the preferences registry.
         * @param defaultValue
         *            The default value for this settings entry, if a value
         *            hasn't already been saved.
         */
        protected Entry(final String node, final A defaultValue) {
            this.node = node;
            this.defaultValue = defaultValue;
        }

        /**
         * Changes the value of this entry.
         * 
         * @param value
         *            The value to change to.
         */
        public abstract void setValue(A value);

        /**
         * Returns the value of this settings entry.
         * 
         * @return The value of this settings entry, or the default value if no
         *         value is present.
         */
        public abstract A getValue();
    }

    protected class StringEntry extends Entry<String> {
        public StringEntry(final String node, final String defaultValue) {
            super(node, defaultValue);
        }

        @Override
        public void setValue(final String value) {
            Settings.this.prefs.put(this.node, value);
        }

        @Override
        public String getValue() {
            return Settings.this.prefs.get(this.node, this.defaultValue);
        }
    }

    protected class IntegerEntry extends Entry<Integer> {
        protected IntegerEntry(final String node, final Integer defaultValue) {
            super(node, defaultValue);
        }

        @Override
        public void setValue(final Integer value) {
            Settings.this.prefs.putInt(this.node, value);
        }

        @Override
        public Integer getValue() {
            return Settings.this.prefs.getInt(this.node, this.defaultValue);
        }
    }

    protected class BooleanEntry extends Entry<Boolean> {

        protected BooleanEntry(final String node, final Boolean defaultValue) {
            super(node, defaultValue);
        }

        @Override
        public void setValue(final Boolean value) {
            Settings.this.prefs.putBoolean(this.node, value);
        }

        @Override
        public Boolean getValue() {
            return Settings.this.prefs.getBoolean(this.node, this.defaultValue);
        }

    }

    protected class RendererEntry extends Entry<RendererImpl> {

        protected RendererEntry(final String node,
                final RendererImpl defaultValue) {
            super(node, defaultValue);
        }

        @Override
        public void setValue(final RendererImpl value) {
            Settings.this.prefs.put(this.node, value.name());
        }

        @Override
        public RendererImpl getValue() {
            return RendererImpl.valueOf(Settings.this.prefs.get(this.node,
                    this.defaultValue.name()));
        }

    }

    public final Entry<Integer> viewportWidth = new IntegerEntry(
            "graphics/viewport/width", 800);
    public final Entry<Integer> viewportHeight = new IntegerEntry(
            "graphics/viewport/height", 600);
    public final Entry<Integer> viewportDepth = new IntegerEntry(
            "graphics/viewport/color_depth", 32);
    public final Entry<Integer> viewportDepthBufferBits = new IntegerEntry(
            "graphics/viewport/depth_buffer_bits", 8);
    public final Entry<Integer> viewportAlphaBufferBits = new IntegerEntry(
            "graphics/viewport/alpha_buffer_bits", 0);
    public final Entry<Integer> viewportStencilBufferBits = new IntegerEntry(
            "graphics/viewport/stencil_buffer_bits", 0);
    public final Entry<Integer> screenFrequency = new IntegerEntry(
            "graphics/screen/frequency", 60);
    public final Entry<Boolean> screenFullscreen = new BooleanEntry(
            "graphics/screen/fullscreen", false);
    public final Entry<Boolean> screenVerticalSync = new BooleanEntry(
            "graphics/screen/vertical_sync", false);
    public final Entry<Integer> screenSamplesPerPixel = new IntegerEntry(
            "graphics/samples_per_pixel", 0);
    public final Entry<RendererImpl> rendererImpl = new RendererEntry(
            "graphics/renderer", RendererImpl.LWJGL);
    public final Entry<Integer> framerate = new IntegerEntry(
            "graphics/framerate", -1);
    public final Entry<Boolean> stereoscopic = new BooleanEntry(
            "graphics/stereoscopic", false);
    public final Entry<Boolean> musicEnabled = new BooleanEntry(
            "audio/music/enabled", true);
    public final Entry<Boolean> soundEffectsEnabled = new BooleanEntry(
            "audio/effects/enabled", true);
}
