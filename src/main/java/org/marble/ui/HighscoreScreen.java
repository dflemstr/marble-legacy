package org.marble.ui;

import java.util.Map.Entry;

import de.lessvoid.nifty.controls.dynamic.CustomControlCreator;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;

import org.marble.Game;
import org.marble.level.MetaLevel;
import org.marble.level.StatisticalMetaLevel;

public class HighscoreScreen extends AbstractScreenController {

    public HighscoreScreen(final Game game) {
        super(game);
    }

    @Override
    public void onGoto() {
        if (game.getCurrentLevel().isPresent()) {
            final MetaLevel currentLevel = game.getCurrentLevel().get();
            final StatisticalMetaLevel stats =
                    game.getSettings().levelStatistics.getEntry(
                            currentLevel.getUUID()).getValue();

            screen.findElementByName("level-name")
                    .getRenderer(TextRenderer.class)
                    .setText(currentLevel.getName());

            final Element highscoreList =
                    screen.findElementByName("highscore-list");
            highscoreList.getElements().clear();

            for (final Entry<String, Integer> entry : stats.getHighscores()
                    .entrySet()) {

                final CustomControlCreator highscoreEntryCreator =
                        new CustomControlCreator("highscore-entry");
                final Element highscoreEntry =
                        highscoreEntryCreator.create(nifty, screen,
                                highscoreList);
                highscoreEntry.findElementByName("highscore-text")
                        .getRenderer(TextRenderer.class)
                        .setText(entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    public void goBack() {
        game.gotoScreen(UIScreen.Win);
    }
}
