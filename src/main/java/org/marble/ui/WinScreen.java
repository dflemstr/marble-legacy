package org.marble.ui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;

import org.marble.Game;

public class WinScreen extends AbstractScreenController {

    public WinScreen(final Game game) {
        super(game);
    }

    @Override
    public void bind(final Nifty nifty, final Screen screen) {
        super.bind(nifty, screen);
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
        nifty.gotoScreen("game");
    }

    public void retry() {
        if (game.getCurrentLevel().isPresent()) {
            game.loadLevel(game.getCurrentLevel().get());
            nifty.gotoScreen("game");
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
