package org.marble.ui;

import java.net.MalformedURLException;
import java.net.URL;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;

import org.marble.Game;

public class LevelPackScreen extends AbstractScreenController {

    public LevelPackScreen(final Game game) {
        super(game);
    }

    @Override
    public void bind(final Nifty nifty, final Screen screen) {
        super.bind(nifty, screen);
        screen.findNiftyControl("level-pack-url", TextField.class).setText(
                game.getCurrentLevelPackURL().toExternalForm());
    }

    public void loadLevelPack() {
        try {
            game.loadLevelPack(new URL(screen.findNiftyControl(
                    "level-pack-url", TextField.class).getText()));
        } catch (final MalformedURLException e) {
            game.handleError("Could not load level pack", e);
        }
    }

    public void goBack() {
        nifty.gotoScreen("start");
    }
}
