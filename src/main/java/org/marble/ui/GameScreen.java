package org.marble.ui;

import com.jme3.math.Vector3f;

import de.lessvoid.nifty.elements.render.TextRenderer;

import org.marble.Game;
import org.marble.ball.BallKind;
import org.marble.session.GameSession;
import org.marble.session.GameSessionListener;

public class GameScreen extends AbstractScreenController {
    private GameSession currentSession;

    private final class GameScreenUpdater implements GameSessionListener {
        @Override
        public void changedLives(final int lives) {
            updateStats(lives, (int) currentSession.getPoints());
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
        public void changedPauseState(final GameSession.PauseState pauseState) {
            // Do nothing
        }

        @Override
        public void changedRespawnPoint(final Vector3f respawnPoint) {
            // Do nothing
        }

        @Override
        public void changedRespawnKind(final BallKind respawnKind) {
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
    public void onGoto() {
        if (game.getCurrentSession().isPresent()) {
            currentSession = game.getCurrentSession().get();
            currentSession.addGameSessionListener(new GameScreenUpdater());
            updateStats(currentSession.getLives(),
                    (int) currentSession.getPoints());
        }
    }
}
