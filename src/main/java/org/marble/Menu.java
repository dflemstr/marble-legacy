package org.marble;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIComboBox;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.UITabbedPane;
import com.ardor3d.extension.ui.UITabbedPane.TabPlacement;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.event.SelectionListener;
import com.ardor3d.extension.ui.layout.BorderLayout;
import com.ardor3d.extension.ui.layout.BorderLayoutData;
import com.ardor3d.extension.ui.layout.GridLayout;
import com.ardor3d.extension.ui.layout.GridLayoutData;
import com.ardor3d.extension.ui.model.DefaultComboBoxModel;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.resource.ResourceLocatorTool;

import com.google.common.collect.ImmutableSet;

import org.json.JSONException;

import org.marble.entity.Entity;
import org.marble.level.LevelLoader;
import org.marble.level.MetaLevel;
import org.marble.level.MetaLevelPack;

public class Menu {
    private final UIHud hud;
    private final UIFrame frame;

    Game game;

    public Menu(final Game game) throws Exception {

        this.game = game;
        final UIPanel widget = makeWidgetPanel();
        final UIPanel settings = makeSettingsPanel();
        final UIPanel levels = makeLevelsPanel();
        final UITabbedPane pane = new UITabbedPane(TabPlacement.NORTH);
        pane.add(widget, "widget");
        pane.add(levels, "levels");
        pane.add(settings, "settings");
        frame = new UIFrame("UI Sample");
        frame.setContentPanel(pane);
        frame.updateMinimumSizeFromContents();
        frame.layout();
        frame.pack();
        frame.setContentPanel(pane);
        frame.setUseStandin(true);
        frame.setOpacity(1f);
        frame.setName("sample");
        frame.setLocationRelativeTo(game.getGraphicsEngine().getCanvas()
                .getCanvasRenderer().getCamera());

        hud = new UIHud();
        hud.add(frame);
        hud.setupInput(game.getGraphicsEngine().getCanvas(), game
                .getInputEngine().getPhysicalLayer(), game.getInputEngine()
                .getLogicalLayer());
        hud.setMouseManager(game.getInputEngine().getMouseManager());
    }

    public final Spatial getSpatial() {
        return hud;
    }

    private UIPanel makeWidgetPanel() {

        final UIPanel panel = new UIPanel();
        panel.setForegroundColor(ColorRGBA.DARK_GRAY);
        panel.setLayout(new BorderLayout());

        final UIButton button = new UIButton("Restart");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                game.restart();

            }
        });
        button.setGap(10);
        button.setLayoutData(BorderLayoutData.NORTH);
        button.setTooltipText("This is a tooltip!");
        panel.add(button);
        return panel;
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
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

    private UIPanel makeSettingsPanel() throws Exception {
        final UIPanel panel = new UIPanel();
        panel.setForegroundColor(ColorRGBA.DARK_GRAY);
        panel.setLayout(new BorderLayout());
        final DisplayMode[] modes = getResolutions();
        final List<String> resolutions = new ArrayList<String>(modes.length);
        for (final DisplayMode mode : modes) {
            final String res = mode.getWidth() + " x " + mode.getHeight();
            if (!resolutions.contains(res)) {
                resolutions.add(res);
            }
        }

        final String[] res = new String[resolutions.size()];
        resolutions.toArray(res);
        final DefaultComboBoxModel test = new DefaultComboBoxModel();
        for (final String re : res) {
            test.addItem(re);
        }

        final UIComboBox resolutionBox = new UIComboBox(test);
        resolutionBox.addSelectionListener(new SelectionListener<UIComboBox>() {

            @Override
            public void selectionChanged(final UIComboBox comboBox,
                    final Object newValue) {
                final String value = (String) newValue;
                final StringTokenizer token = new StringTokenizer(value);
                final String width = token.nextToken();
                token.nextToken();
                final String height = token.nextToken();
                game.getSettings().viewportHeight.setValue(Integer
                        .parseInt(height));
                game.getSettings().viewportWidth.setValue(Integer
                        .parseInt(width));
                game.restart();
            }

        });

        // Find current setting

        int index = -1;

        for (int i = 0; i < res.length; i++) {
            final String resolution =
                    game.getSettings().viewportWidth.getValue() + " x "
                            + game.getSettings().viewportHeight.getValue();
            if (res[i].equals(resolution)) {
                index = i;
                break;
            }
        }

        if (index == -1)
            throw new Exception(
                    "Saved resolution not supported, forcing a reset..");

        resolutionBox.setSelectedIndex(index);
        panel.add(resolutionBox);
        return panel;
    }

    private DisplayMode[] getResolutions() {
        final DisplayMode[] modes =
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDisplayModes();
        return modes;
    }

    public void update(final ReadOnlyTimer timer) {
        hud.getLogicalLayer().checkTriggers(timer.getTimePerFrame());
        hud.updateGeometricState(timer.getTimePerFrame());
    }

    public void render(final Renderer renderer) {
        renderer.renderBuckets();
        renderer.draw(hud);
    }
}
