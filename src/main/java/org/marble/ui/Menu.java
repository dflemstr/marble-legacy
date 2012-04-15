package org.marble.ui;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIComboBox;
import com.ardor3d.extension.ui.UIComponent;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.UITabbedPane;
import com.ardor3d.extension.ui.UITabbedPane.TabPlacement;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.layout.BorderLayout;
import com.ardor3d.extension.ui.layout.BorderLayoutData;
import com.ardor3d.extension.ui.layout.GridLayout;
import com.ardor3d.extension.ui.layout.GridLayoutData;
import com.ardor3d.extension.ui.model.ComboBoxModel;
import com.ardor3d.extension.ui.model.DefaultComboBoxModel;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.util.resource.ResourceLocatorTool;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;

import org.marble.Game;
import org.marble.entity.Entity;
import org.marble.level.LevelLoader;
import org.marble.level.MetaLevel;
import org.marble.level.MetaLevelPack;
import org.marble.ui.settings.ComboBoxSettingsWidget;

public class Menu extends UIFrame {
    private final Game game;

    public Menu(final Game game) {
        super("Main menu");
        this.game = game;
        final UIPanel widget = makeWidgetPanel();
        final UIPanel settings = makeSettingsPanel();
        final UIPanel levels = makeLevelsPanel();

        final UITabbedPane pane = new UITabbedPane(TabPlacement.NORTH);
        pane.add(widget, "Main menu");
        pane.add(levels, "Levels");
        pane.add(settings, "Settings");

        setContentPanel(pane);
        updateMinimumSizeFromContents();
        layout();
        pack();
        setUseStandin(true);
        setOpacity(1f);
        setName("sample");
    }

    @Override
    public void close() {
        setVisible(false);
    }

    private static <A> ComboBoxModel createAlternativesModel(
            final Iterable<A> alternatives, final Descriptor<A> descriptor) {
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        int i = 0;
        for (final A value : alternatives) {
            model.addItem(i, value);
            model.setViewAt(i, descriptor.getName(value));
            final Optional<String> description =
                    descriptor.getDescription(value);
            if (description.isPresent()) {
                model.setToolTipAt(i, description.get());
            }
            i++;
        }
        return model;
    }

    private static <A extends Enum<A>> ComboBoxModel createEnumModel(
            final Class<A> enumType, final Descriptor<A> descriptor) {
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        final A[] values = enumType.getEnumConstants();
        for (int i = 0; i < values.length; i++) {
            model.setValueAt(i, values[i]);
            model.setViewAt(i, descriptor.getName(values[i]));
            final Optional<String> description =
                    descriptor.getDescription(values[i]);
            if (description.isPresent()) {
                model.setToolTipAt(i, description.get());
            }
        }
        return model;
    }

    private static ComboBoxModel createToggleModel() {
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.setValueAt(0, Boolean.TRUE);
        model.setViewAt(0, "Enabled");
        model.setToolTipAt(0, "Enable this setting");
        model.setValueAt(0, Boolean.FALSE);
        model.setViewAt(0, "Disabled");
        model.setToolTipAt(0, "Disable this setting");
        return model;
    }

    private UIPanel makeLevelsPanel() {

        final UIPanel panel = new UIPanel(new GridLayout());
        panel.setForegroundColor(ColorRGBA.DARK_GRAY);

        final LevelLoader loader = new LevelLoader();

        final String LEVEL_DIR =
                Game.class.getPackage().getName().replace('.', '/') + "/level/";

        MetaLevelPack pack = null;
        try {
            final URL url =
                    ResourceLocatorTool.getClassPathResource(Game.class,
                            LEVEL_DIR + "core.pack");
            pack = loader.loadMetaLevelPack(url);
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final JSONException e) {
            e.printStackTrace();
        }

        final UILabel creator =
                new UILabel("Creator: " + pack.getAuthor().or("Unknown"));
        creator.setLayoutData(new GridLayoutData(2, true, true));
        final UILabel description =
                new UILabel("Description: "
                        + pack.getDescription().or("Unknown"));
        description.setLayoutData(new GridLayoutData(2, true, true));
        final UILabel name = new UILabel("Name: " + pack.getName());
        name.setLayoutData(new GridLayoutData(2, true, true));
        final UILabel version =
                new UILabel("Version: " + pack.getVersion().or("Unknown"));
        version.setLayoutData(new GridLayoutData(2, true, true));
        panel.add(new UILabel("Level pack:"));
        panel.add(name);
        panel.add(description);
        panel.add(creator);
        panel.add(version);

        for (final MetaLevel metaLevel : pack.getLevels()) {
            final String levelName = metaLevel.getName();
            final UIButton button = new UIButton(levelName);
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    // Load level
                    final URL url = metaLevel.getUri();
                    try {
                        final ImmutableSet<Entity> level =
                                loader.loadLevel(url);
                        game.load(level);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            panel.add(button);
        }
        return panel;
    }

    private int computeVariance(final Pair<Integer, Integer> pair1,
            final Pair<Integer, Integer> pair2) {
        return (pair1.getLeft() - pair2.getLeft())
                * (pair1.getLeft() - pair2.getLeft())
                + (pair1.getRight() - pair2.getRight())
                * (pair1.getRight() - pair2.getRight());
    }

    private Pair<Integer, Integer> findClosestResolution(
            final Pair<Integer, Integer> currentResolution,
            final Collection<Pair<Integer, Integer>> resolutions) {
        final Iterator<Pair<Integer, Integer>> iter = resolutions.iterator();
        assert iter.hasNext();
        final Pair<Integer, Integer> firstResolution = iter.next();

        int score = computeVariance(currentResolution, firstResolution);
        Pair<Integer, Integer> closestResolution = firstResolution;

        while (iter.hasNext()) {
            final Pair<Integer, Integer> candidate = iter.next();
            final int newScore = computeVariance(currentResolution, candidate);
            if (score > newScore) {
                score = newScore;
                closestResolution = candidate;
            }
        }

        return closestResolution;
    }

    private void addSettingsRow(final UIPanel panel, final String label,
            final UIComponent component) {
        final UILabel labelWidget = new UILabel(label);
        component.setLayoutData(GridLayoutData.Wrap);
        panel.add(labelWidget);
        panel.add(component);
    }

    private UIPanel makeSettingsPanel() {
        final UIPanel panel = new UIPanel();
        panel.setLayout(new GridLayout());
        final DisplayMode[] modes =
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDisplayModes();

        final ImmutableMultimap.Builder<Pair<Integer, Integer>, DisplayMode> resolutionsBuilder =
                ImmutableMultimap.builder();
        for (final DisplayMode mode : modes) {
            final Pair<Integer, Integer> resolution =
                    Pair.of(mode.getWidth(), mode.getHeight());
            resolutionsBuilder.put(resolution, mode);
        }
        final ImmutableMultimap<Pair<Integer, Integer>, DisplayMode> resolutions =
                resolutionsBuilder.build();

        final Pair<Integer, Integer> savedResolution =
                game.getSettings().viewportResolution.getValue();
        final Pair<Integer, Integer> currentResolution =
                resolutions.containsKey(savedResolution) ? savedResolution
                        : findClosestResolution(savedResolution,
                                resolutions.keySet());
        game.getSettings().viewportResolution.setValue(currentResolution);

        final ComboBoxModel resolutionModel =
                createAlternativesModel(resolutions.keySet(),
                        new ResolutionDescriptor());
        final UIComboBox resolutionComboBox =
                new ComboBoxSettingsWidget<Pair<Integer, Integer>>(
                        game.getSettings().viewportResolution, resolutionModel);
        addSettingsRow(panel, "Resolution", resolutionComboBox);
        return panel;
    }

    private UIPanel makeWidgetPanel() {

        final UIPanel panel = new UIPanel();
        panel.setForegroundColor(ColorRGBA.DARK_GRAY);
        panel.setLayout(new BorderLayout());

        final UIButton button = new UIButton("Restart");
        button.addActionListener(new RestartGame());
        button.setGap(10);
        button.setLayoutData(BorderLayoutData.NORTH);
        button.setTooltipText("Restart the game");
        panel.add(button);
        return panel;
    }

    private final class ResolutionDescriptor implements
            Descriptor<Pair<Integer, Integer>> {
        @Override
        public String getName(final Pair<Integer, Integer> a) {
            return a.getLeft() + "\u00d7" + a.getRight();
        }

        @Override
        public Optional<String> getDescription(final Pair<Integer, Integer> a) {
            return Optional.<String> absent();
        }
    }

    private final class RestartGame implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent arg0) {
            game.restart();
        }
    }
}
