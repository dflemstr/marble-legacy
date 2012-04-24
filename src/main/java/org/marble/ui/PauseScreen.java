package org.marble.ui;

import org.marble.Game;

public class PauseScreen extends AbstractScreenController {
    public PauseScreen(final Game game) {
        super(game);
    }

    public void resume() {
        game.resume();
    }
}
