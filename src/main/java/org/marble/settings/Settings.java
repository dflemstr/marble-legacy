package org.marble.settings;

import java.util.UUID;
import java.util.prefs.Preferences;


import org.apache.commons.lang3.tuple.Pair;

import org.marble.level.StatisticalMetaLevel;
import org.marble.settings.specialized.BooleanEntry;
import org.marble.settings.specialized.CompositeEntry;
import org.marble.settings.specialized.EnumEntry;
import org.marble.settings.specialized.IntegerEntry;
import org.marble.settings.specialized.StatisticsEntryMap;
import org.marble.util.Quality;

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
    public final Entry<Integer> framerate = new IntegerEntry(prefs,
            "graphics/framerate", -1);
    public final Entry<Boolean> stereoscopic = new BooleanEntry(prefs,
            "graphics/stereoscopic", false);
    public final Entry<Quality> environmentQuality = new EnumEntry<Quality>(
            prefs, "graphics/environment_quality", Quality.Medium,
            Quality.class);
    public final Entry<Boolean> bloom = new BooleanEntry(prefs,
            "graphics/bloom", true);
    public final Entry<Boolean> ssao = new BooleanEntry(prefs, "graphics/ssao",
            true);
    public final Entry<Quality> ssaoQuality = new EnumEntry<Quality>(prefs,
            "graphics/ssao/quality", Quality.Highest, Quality.class);
    public final Entry<Boolean> dof = new BooleanEntry(prefs, "graphics/dof",
            false);
    public final Entry<Boolean> dofVignetting = new BooleanEntry(prefs,
            "graphics/dof/vignetting", false);
    public final Entry<Boolean> dofDepthBlur = new BooleanEntry(prefs,
            "graphics/dof/depth_blur", false);
    public final Entry<Boolean> dofPentagonBokeh = new BooleanEntry(prefs,
            "graphics/dof/pentagon_bokeh", false);
    public final Entry<Quality> dofQuality = new EnumEntry<Quality>(prefs,
            "graphics/dof/quality", Quality.Medium, Quality.class);
    public final Entry<Boolean> musicEnabled = new BooleanEntry(prefs,
            "audio/music/enabled", true);
    public final Entry<Boolean> soundEffectsEnabled = new BooleanEntry(prefs,
            "audio/effects/enabled", true);

    public final EntryMap<UUID, StatisticalMetaLevel> levelStatistics =
            new StatisticsEntryMap(prefs, "statistics");

    public final Entry<Pair<Integer, Integer>> viewportResolution =
            new CompositeEntry<Integer, Integer>(viewportWidth, viewportHeight);
}
