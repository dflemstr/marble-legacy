package org.marble.session;

import java.util.Set;

import com.jme3.math.Vector3f;

import com.google.common.collect.Sets;

import org.marble.ball.BallKind;

public class GameSession {
    public final int STARTING_LIVES = 3;
    public final int STARTING_POINTS = 1000;

    private final Set<GameSessionListener> listeners = Sets.newHashSet();
    private int lives = STARTING_LIVES;
    private PauseState pauseState = PauseState.Running;
    private float points = STARTING_POINTS;
    private BallKind respawnKind = BallKind.Wood;
    private final Vector3f respawnPoint = new Vector3f(0, 0, 2);

    public void addGameSessionListener(final GameSessionListener listener) {
        listeners.add(listener);
    }

    public int getLives() {
        return lives;
    }

    public PauseState getPauseState() {
        return pauseState;
    }

    public float getPoints() {
        return points;
    }

    /**
     * @return the respawnKind
     */
    public BallKind getRespawnKind() {
        return respawnKind;
    }

    public Vector3f getRespawnPoint() {
        return respawnPoint;
    }

    public void removeGameSessionListener(final GameSessionListener listener) {
        listeners.remove(listener);
    }

    public void setLives(final int lives) {
        if (this.lives != lives) {
            this.lives = lives;
            for (final GameSessionListener listener : listeners) {
                listener.changedLives(lives);
            }
        }
    }

    public void setPauseState(final PauseState pauseState) {
        if (this.pauseState != pauseState) {
            this.pauseState = pauseState;

            for (final GameSessionListener listener : listeners) {
                listener.changedPauseState(pauseState);
            }
        }
    }

    public void setPoints(final float f) {
        if (points != f) {
            points = f;
            for (final GameSessionListener listener : listeners) {
                listener.changedPoints((int) f);
            }
        }
    }

    /**
     * @param respawnKind
     *            the respawnKind to set
     */
    public void setRespawnKind(final BallKind respawnKind) {
        this.respawnKind = respawnKind;
    }

    public void setRespawnPoint(final Vector3f respawnPoint) {
        if (!this.respawnPoint.equals(respawnPoint)) {
            this.respawnPoint.set(respawnPoint);
            for (final GameSessionListener listener : listeners) {
                listener.changedRespawnPoint(respawnPoint);
            }
        }
    }

    public enum PauseState {
        EnforcedPause, PlayerPaused, Running;
    }
}
