package org.marble.settings;

import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.lang3.tuple.Pair;

import org.marble.Game;
import org.marble.frp.mutable.CompositeMutableReactive;
import org.marble.frp.mutable.MutableReactive;
import org.marble.level.StatisticalMetaLevel;
import org.marble.settings.specialized.BooleanEntry;
import org.marble.settings.specialized.EnumEntry;
import org.marble.settings.specialized.IntegerEntry;
import org.marble.settings.specialized.StatisticsEntryMap;
import org.marble.settings.specialized.StringEntry;
import org.marble.util.Quality;

/**
 * Controls the settings for the desktop version of the application.
 */
public class Settings {
    protected final Preferences prefs = Preferences
            .userNodeForPackage(Game.class);

    public final MutableReactive<Integer> viewportWidth = new IntegerEntry(
            prefs, "graphics/viewport/width", 800);
    public final MutableReactive<Integer> viewportHeight = new IntegerEntry(
            prefs, "graphics/viewport/height", 600);
    public final MutableReactive<Integer> viewportDepth = new IntegerEntry(
            prefs, "graphics/viewport/color_depth", 32);
    public final MutableReactive<Integer> viewportDepthBufferBits =
            new IntegerEntry(prefs, "graphics/viewport/depth_buffer_bits", 8);
    public final MutableReactive<Integer> viewportAlphaBufferBits =
            new IntegerEntry(prefs, "graphics/viewport/alpha_buffer_bits", 0);
    public final MutableReactive<Integer> viewportStencilBufferBits =
            new IntegerEntry(prefs, "graphics/viewport/stencil_buffer_bits", 0);
    public final MutableReactive<Integer> screenFrequency = new IntegerEntry(
            prefs, "graphics/screen/frequency", 60);
    public final MutableReactive<Boolean> screenFullscreen = new BooleanEntry(
            prefs, "graphics/screen/fullscreen", false);
    public final MutableReactive<Boolean> screenVerticalSync =
            new BooleanEntry(prefs, "graphics/screen/vertical_sync", false);
    public final MutableReactive<Integer> screenSamplesPerPixel =
            new IntegerEntry(prefs, "graphics/samples_per_pixel", 0);
    public final MutableReactive<Integer> framerate = new IntegerEntry(prefs,
            "graphics/framerate", 60);
    public final MutableReactive<Boolean> stereoscopic = new BooleanEntry(
            prefs, "graphics/stereoscopic", false);
    public final MutableReactive<Quality> environmentQuality =
            new EnumEntry<Quality>(prefs, "graphics/environment_quality",
                    Quality.Medium, Quality.class);
    public final MutableReactive<Boolean> bloom = new BooleanEntry(prefs,
            "graphics/bloom", true);
    public final MutableReactive<Boolean> ssao = new BooleanEntry(prefs,
            "graphics/ssao", true);
    public final MutableReactive<Quality> ssaoQuality = new EnumEntry<Quality>(
            prefs, "graphics/ssao/quality", Quality.Highest, Quality.class);
    public final MutableReactive<Boolean> dof = new BooleanEntry(prefs,
            "graphics/dof", true);
    public final MutableReactive<Boolean> dofVignetting = new BooleanEntry(
            prefs, "graphics/dof/vignetting", false);
    public final MutableReactive<Boolean> dofDepthBlur = new BooleanEntry(
            prefs, "graphics/dof/depth_blur", true);
    public final MutableReactive<Boolean> dofPentagonBokeh = new BooleanEntry(
            prefs, "graphics/dof/pentagon_bokeh", false);
    public final MutableReactive<Quality> dofQuality = new EnumEntry<Quality>(
            prefs, "graphics/dof/quality", Quality.Medium, Quality.class);
    public final MutableReactive<Boolean> musicEnabled = new BooleanEntry(
            prefs, "audio/music/enabled", true);
    public final MutableReactive<Boolean> soundEffectsEnabled =
            new BooleanEntry(prefs, "audio/effects/enabled", true);
    public final MutableReactive<String> playerName = new StringEntry(prefs,
            "player/name", "Player");

    public final EntryMap<UUID, StatisticalMetaLevel> levelStatistics =
            new StatisticsEntryMap(prefs, "statistics");

    public final MutableReactive<Pair<Integer, Integer>> viewportResolution =
            new CompositeMutableReactive<Integer, Integer>(viewportWidth,
                    viewportHeight);

    public void sync() throws BackingStoreException {
        prefs.sync();
    }
}
