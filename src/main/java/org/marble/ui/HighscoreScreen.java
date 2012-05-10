package org.marble.ui;

import java.util.Comparator;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableSortedSet;

import de.lessvoid.nifty.controls.dynamic.CustomControlCreator;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;

import org.marble.Game;
import org.marble.level.MetaLevel;
import org.marble.level.StatisticalMetaLevel;

public class HighscoreScreen extends AbstractScreenController {
    private final Comparator<Entry<String, Integer>> highscoreComparator =
            new Comparator<Entry<String, Integer>>() {
                @Override
                public int compare(final Entry<String, Integer> o1,
                        final Entry<String, Integer> o2) {
                    return -o1.getValue().compareTo(o2.getValue());
                }

            };

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
            System.out.println("Loaded: " + stats.getHighscores());

            screen.findElementByName("level-name")
                    .getRenderer(TextRenderer.class)
                    .setText(currentLevel.getName());

            final Element highscoreList =
                    screen.findElementByName("highscore-list");
            highscoreList.getElements().clear();
            final ImmutableSortedSet<Entry<String, Integer>> highscores =
                    ImmutableSortedSet.copyOf(highscoreComparator, stats
                            .getHighscores().entrySet());
            for (final Entry<String, Integer> entry : highscores) {

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
