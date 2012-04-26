package org.marble.ui;

import de.lessvoid.nifty.elements.Element;

import org.marble.Game;

public class WinScreen extends AbstractScreenController {

    public WinScreen(final Game game) {
        super(game);
    }

    @Override
    public void onGoto() {
        if (!game.hasNextLevel()) {
            final Element button =
                    screen.findElementByName("next-level-button");
            final Element parent = button.getParent();
            parent.getElements().remove(button);
            parent.layoutElements();
        }
    }

    public void gotoNextLevel() {
        game.loadNextLevel();
        game.gotoScreen(UIScreen.Game);
    }

    public void retry() {
        if (game.getCurrentLevel().isPresent()) {
            game.playLevel(game.getCurrentLevel().get());
        } else
            throw new RuntimeException("The current level has disappeared");
    }

    public void showHighscores() {
        if (game.getCurrentLevel().isPresent()) {
            game.showHighscores(game.getCurrentLevel().get());
        } else
            throw new RuntimeException("The current level has disappeared");
    }

    public void gotoMenu() {
        game.gotoMenu();
    }
}
