package org.marble.ui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

import org.marble.Game;

public class AbstractScreenController implements ScreenController {
    protected final Game game;
    protected Nifty nifty;
    protected Screen screen;

    public AbstractScreenController(final Game game) {
        this.game = game;
    }

    @Override
    public void bind(final Nifty nifty, final Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    @Override
    public void onEndScreen() {
        // Do nothing
    }

    public void onGoto() {
        // Do nothing
    }

    @Override
    public void onStartScreen() {
        nifty.resolutionChanged();
        onGoto(); // TODO call this earlier
    }
}
