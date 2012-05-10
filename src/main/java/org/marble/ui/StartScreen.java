package org.marble.ui;

import org.marble.Game;

public class StartScreen extends AbstractScreenController {
    public StartScreen(final Game game) {
        super(game);
    }

    public void exit() {
        game.stop();
    }

    public void loadLevel() {
        nifty.gotoScreen("levels");
    }

    public void showSettings() {
        nifty.gotoScreen("settings");
    }

    public void switchLevelPack() {
        nifty.gotoScreen("level-packs");
    }
}
