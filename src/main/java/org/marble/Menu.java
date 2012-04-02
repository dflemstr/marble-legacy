package org.marble;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIComboBox;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.UITabbedPane;
import com.ardor3d.extension.ui.UITabbedPane.TabPlacement;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.event.SelectionListener;
import com.ardor3d.extension.ui.layout.BorderLayout;
import com.ardor3d.extension.ui.layout.BorderLayoutData;
import com.ardor3d.extension.ui.model.DefaultComboBoxModel;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.ReadOnlyTimer;

public class Menu {
    private final UIHud hud;
    private final UIFrame frame;

    Game game;

    public Menu(final Game game) {

        this.game = game;
        final UIPanel widget = makeWidgetPanel();
        final UIPanel settings = makeSettingsPanel();
        final UITabbedPane pane = new UITabbedPane(TabPlacement.NORTH);
        pane.add(widget, "widget");
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

    private UIPanel makeSettingsPanel() {
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
