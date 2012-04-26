package org.marble.ui;

import org.marble.Game;

public class LossScreen extends AbstractScreenController {

    public LossScreen(final Game game) {
        super(game);
    }

    public void retry() {
        if (game.getCurrentLevel().isPresent()) {
            game.playLevel(game.getCurrentLevel().get());
        } else
            throw new RuntimeException("The current level has disappeared");
    }

    public void gotoMenu() {
        game.gotoMenu();
    }
}
