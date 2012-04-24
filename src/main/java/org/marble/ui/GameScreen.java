package org.marble.ui;

import com.jme3.math.Vector3f;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;

import org.marble.Game;
import org.marble.session.GameSession;
import org.marble.session.GameSessionListener;

public class GameScreen extends AbstractScreenController {
    private GameSession currentSession;

    private final class GameScreenUpdater implements GameSessionListener {
        @Override
        public void changedLives(final int lives) {
            updateStats(lives, currentSession.getPoints());
        }

        @Override
        public void changedPoints(final int points) {
            updateStats(currentSession.getLives(), points);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof GameScreenUpdater;
        }

        @Override
        public int hashCode() {
            return 37063;
        }

        @Override
        public void changedPaused(final GameSession.PauseState paused) {
            // Do nothing
        }

        @Override
        public void changedRespawnPoint(final Vector3f respawnPoint) {
            // Do nothing
        }
    }

    public GameScreen(final Game game) {
        super(game);
    }

    private void updateStats(final int lives, final int points) {
        screen.findElementByName("stats-counter")
                .getRenderer(TextRenderer.class)
                .setText("Lives: " + lives + "\nPoints: " + points);
    }

    @Override
    public void bind(final Nifty nifty, final Screen screen) {
        super.bind(nifty, screen);
        if (game.getCurrentSession().isPresent()) {
            currentSession = game.getCurrentSession().get();
            currentSession.addGameSessionListener(new GameScreenUpdater());
            updateStats(currentSession.getLives(), currentSession.getPoints());
        }
    }
}
