package org.marble.ui;

import java.util.UUID;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyMethodInvoker;
import de.lessvoid.nifty.controls.dynamic.CustomControlCreator;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;

import org.marble.Game;
import org.marble.level.MetaLevel;
import org.marble.level.MetaLevelPack;

public class LevelScreen extends AbstractScreenController {
    public LevelScreen(final Game game) {
        super(game);
    }

    @Override
    public void bind(final Nifty nifty, final Screen screen) {
        super.bind(nifty, screen);
        screen.findElementByName("level-pack-name")
                .getRenderer(TextRenderer.class).setText(getLevelPackName());
        screen.findElementByName("level-pack-description")
                .getRenderer(TextRenderer.class)
                .setText(getLevelPackDescription());

        final Element levelList = screen.findElementByName("level-list");
        levelList.getElements().clear();
        for (final MetaLevel level : game.getCurrentLevelPack().getLevels()) {
            final CustomControlCreator levelButtonCreator =
                    new CustomControlCreator("level-button");
            final Element levelButton =
                    levelButtonCreator.create(nifty, screen, levelList);
            levelButton.findElementByName("level-name")
                    .getRenderer(TextRenderer.class).setText(level.getName());
            levelButton
                    .getElementInteraction()
                    .getPrimary()
                    .setOnClickMethod(
                            new NiftyMethodInvoker(nifty, "loadLevel("
                                    + level.getUUID().toString() + ")", this));
        }
    }

    public String getLevelPackName() {
        final MetaLevelPack levelPack = game.getCurrentLevelPack();
        final StringBuilder packNameBuilder = new StringBuilder();
        packNameBuilder.append(levelPack.getName());
        if (levelPack.getVersion().isPresent()) {
            packNameBuilder.append(" v" + levelPack.getVersion().get());
        }
        if (levelPack.getAuthor().isPresent()) {
            packNameBuilder.append(" by " + levelPack.getAuthor().get());
        }
        return packNameBuilder.toString();
    }

    public String getLevelPackDescription() {
        return game.getCurrentLevelPack().getDescription().or("");
    }

    public void loadLevel(final String uuidString) {
        final UUID uuid = UUID.fromString(uuidString);
        for (final MetaLevel level : game.getCurrentLevelPack().getLevels()) {
            if (level.getUUID().equals(uuid)) {
                game.loadLevel(level);
                game.gotoScreen(UIScreen.Game);
                break;
            }
        }
    }

    public void goBack() {
        game.gotoScreen(UIScreen.Start);
    }
}
