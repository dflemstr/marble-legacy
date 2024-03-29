package org.marble.ui;

import org.marble.Game;
import org.marble.session.GameSession;

public class PauseScreen extends AbstractScreenController {
    public PauseScreen(final Game game) {
        super(game);
    }

    public void gotoMenu() {
        game.gotoMenu();
    }

    public void resume() {
        game.setPause(GameSession.PauseState.Running);
    }
}
