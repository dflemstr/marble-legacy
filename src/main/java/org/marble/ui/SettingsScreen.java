package org.marble.ui;

import org.marble.Game;

public class SettingsScreen extends AbstractScreenController {

    public SettingsScreen(final Game game) {
        super(game);
    }

    public void goBack() {
        nifty.gotoScreen("start");
    }
}
