package org.marble.ui;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.prefs.BackingStoreException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.TextFieldChangedEvent;

import org.marble.Game;
import org.marble.settings.Settings;
import org.marble.util.Resolution;

public class SettingsScreen extends AbstractScreenController {
    private boolean populatingResolutions = false;
    private boolean settingFullscreen = false;
    private final Settings settings;

    public SettingsScreen(final Game game) {
        super(game);
        settings = game.getSettings();
    }

    public void apply() {
        try {
            game.getSettings().sync();
            game.restartContext();
        } catch (final BackingStoreException e) {
            game.handleError("Could not contact settings backend.\n"
                    + "Settings may not have been saved.", e);
        }
    }

    public void goBack() {
        game.gotoScreen(UIScreen.Start);
    }

    @NiftyEventSubscriber(id = "bloom")
    public void onBloomChanged(final String name,
            final CheckBoxStateChangedEvent e) {
        if (name.equals("bloom")) {
            settings.bloom.setValue(e.isChecked());
        }
    }

    @NiftyEventSubscriber(id = "dof")
    public void onDofChanged(final String name,
            final CheckBoxStateChangedEvent e) {
        if (name.equals("dof")) {
            settings.dof.setValue(e.isChecked());
            screen.findNiftyControl("dofPentagonBokeh", CheckBox.class)
                    .setEnabled(e.isChecked());
        }
    }

    @NiftyEventSubscriber(id = "dofPentagonBokeh")
    public void onDofPentagonBokehChanged(final String name,
            final CheckBoxStateChangedEvent e) {
        if (name.equals("dofPentagonBokeh")) {
            settings.dofPentagonBokeh.setValue(e.isChecked());
        }
    }

    @NiftyEventSubscriber(id = "fullscreen")
    public void onFullscreenChanged(final String name,
            final CheckBoxStateChangedEvent e) {
        if (name.equals("fullscreen") && !settingFullscreen) {
            settings.screenFullscreen.setValue(e.isChecked());

            // Apply display settings
            game.restartContext();
        }
    }

    @Override
    public void onGoto() {
        updateResolutions();

        screen.findNiftyControl("playerName", TextField.class).setText(
                settings.playerName.getValue());

        settingFullscreen = true;
        screen.findNiftyControl("fullscreen", CheckBox.class).setChecked(
                settings.screenFullscreen.getValue());
        settingFullscreen = false;

        screen.findNiftyControl("bloom", CheckBox.class).setChecked(
                settings.bloom.getValue());
        screen.findNiftyControl("ssao", CheckBox.class).setChecked(
                settings.ssao.getValue());
        screen.findNiftyControl("dof", CheckBox.class).setChecked(
                settings.dof.getValue());
        final CheckBox dofPBBox =
                screen.findNiftyControl("dofPentagonBokeh", CheckBox.class);
        dofPBBox.setChecked(settings.dofPentagonBokeh.getValue());
        dofPBBox.setEnabled(settings.dof.getValue());
    }

    @NiftyEventSubscriber(id = "playerName")
    public void onPlayerNameChanged(final String name,
            final TextFieldChangedEvent e) {
        if (name.equals("playerName")) {
            settings.playerName.setValue(e.getText());
        }
    }

    @NiftyEventSubscriber(id = "resolutions")
    public void onResolutionChanged(final String name,
            final DropDownSelectionChangedEvent<Resolution> e) {
        if (name.equals("resolutions") && !populatingResolutions) {
            final Resolution res = e.getSelection();
            final Resolution currentRes =
                    new Resolution(settings.viewportWidth.getValue(),
                            settings.viewportHeight.getValue(),
                            settings.viewportDepth.getValue(),
                            settings.screenFrequency.getValue());
            if (!res.equals(currentRes)) {
                settings.viewportWidth.setValue(res.getWidth());
                settings.viewportHeight.setValue(res.getHeight());
                settings.viewportDepth.setValue(res.getDepth());
                settings.screenFrequency.setValue(res.getFrequency());

                // Apply display settings
                game.restartContext();
            }
        }
    }

    @NiftyEventSubscriber(id = "ssao")
    public void onSsaoChanged(final String name,
            final CheckBoxStateChangedEvent e) {
        if (name.equals("ssao")) {
            settings.ssao.setValue(e.isChecked());
        }
    }

    private Resolution findClosestResolution(
            final ImmutableSet<Resolution> resolutions, final Resolution goal) {
        int currentScore = Integer.MAX_VALUE;
        Resolution currentCandidate = resolutions.iterator().next();

        for (final Resolution resolution : resolutions) {
            final int score =
                    Math.abs(resolution.getWidth() - goal.getWidth())
                            + Math.abs(resolution.getHeight()
                                    - goal.getHeight())
                            + Math.abs(resolution.getDepth() - goal.getDepth())
                            + Math.abs(resolution.getFrequency()
                                    - goal.getFrequency());
            if (currentScore > score) {
                currentScore = score;
                currentCandidate = resolution;
            }
        }

        return currentCandidate;
    }

    private void updateResolutions() {
        @SuppressWarnings("unchecked")
        final DropDown<Resolution> resolutionsBox =
                screen.findNiftyControl("resolutions", DropDown.class);

        final GraphicsDevice device =
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice();

        final ImmutableSortedSet.Builder<Resolution> resolutionBuilder =
                ImmutableSortedSet.naturalOrder();
        for (final DisplayMode mode : device.getDisplayModes()) {
            if (mode.getWidth() < 800 || mode.getHeight() < 600
                    || mode.getBitDepth() < 16) {
                continue;
            }
            resolutionBuilder.add(new Resolution(mode.getWidth(), mode
                    .getHeight(), mode.getBitDepth(), mode.getRefreshRate()));
        }
        final ImmutableSet<Resolution> resolutions = resolutionBuilder.build();

        populatingResolutions = true;
        resolutionsBox.addAllItems(ImmutableList.copyOf(resolutionBuilder
                .build()));

        final Resolution currentRes =
                new Resolution(settings.viewportWidth.getValue(),
                        settings.viewportHeight.getValue(),
                        settings.viewportDepth.getValue(),
                        settings.screenFrequency.getValue());
        resolutionsBox
                .selectItem(findClosestResolution(resolutions, currentRes));
        populatingResolutions = false;
    }
}
