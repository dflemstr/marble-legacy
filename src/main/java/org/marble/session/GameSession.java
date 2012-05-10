package org.marble.session;

import java.util.Set;

import com.jme3.math.Vector3f;

import com.google.common.collect.Sets;

import org.marble.ball.BallKind;

public class GameSession {
    public final int STARTING_LIVES = 3;
    public final int STARTING_POINTS = 1000;

    private float points = STARTING_POINTS;
    private int lives = STARTING_LIVES;
    private final Vector3f respawnPoint = new Vector3f(0, 0, 2);
    private BallKind respawnKind = BallKind.Wood;
    private PauseState pauseState = PauseState.Running;
    private final Set<GameSessionListener> listeners = Sets.newHashSet();

    public float getPoints() {
        return points;
    }

    public void setPoints(final float f) {
        if (points != f) {
            points = f;
            for (final GameSessionListener listener : listeners) {
                listener.changedPoints((int) f);
            }
        }
    }

    public int getLives() {
        return lives;
    }

    public void setLives(final int lives) {
        if (this.lives != lives) {
            this.lives = lives;
            for (final GameSessionListener listener : listeners) {
                listener.changedLives(lives);
            }
        }
    }

    public void setRespawnPoint(final Vector3f respawnPoint) {
        if (!this.respawnPoint.equals(respawnPoint)) {
            this.respawnPoint.set(respawnPoint);
            for (final GameSessionListener listener : listeners) {
                listener.changedRespawnPoint(respawnPoint);
            }
        }
    }

    public Vector3f getRespawnPoint() {
        return respawnPoint;
    }

    public void setPauseState(final PauseState pauseState) {
        if (this.pauseState != pauseState) {
            this.pauseState = pauseState;

            for (final GameSessionListener listener : listeners) {
                listener.changedPauseState(pauseState);
            }
        }
    }

    public void addGameSessionListener(final GameSessionListener listener) {
        listeners.add(listener);
    }

    public void removeGameSessionListener(final GameSessionListener listener) {
        listeners.remove(listener);
    }

    public PauseState getPauseState() {
        return pauseState;
    }

    /**
     * @return the respawnKind
     */
    public BallKind getRespawnKind() {
        return respawnKind;
    }

    /**
     * @param respawnKind
     *            the respawnKind to set
     */
    public void setRespawnKind(final BallKind respawnKind) {
        this.respawnKind = respawnKind;
    }

    public enum PauseState {
        Running, PlayerPaused, EnforcedPause;
    }
}
