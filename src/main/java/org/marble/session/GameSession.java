package org.marble.session;

import java.util.Set;

import com.jme3.math.Vector3f;

import com.google.common.collect.Sets;

public class GameSession {
    private float points;
    private int lives = 3;
    private final Vector3f respawnPoint = new Vector3f(0, 0, 2);
    private PauseState paused = PauseState.Running;
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

    public void setPaused(final PauseState paused) {
        if (this.paused != paused) {
            this.paused = paused;

            for (final GameSessionListener listener : listeners) {
                listener.changedPaused(paused);
            }
        }
    }

    public void addGameSessionListener(final GameSessionListener listener) {
        listeners.add(listener);
    }

    public void removeGameSessionListener(final GameSessionListener listener) {
        listeners.remove(listener);
    }

    public PauseState isPaused() {
        return paused;
    }

    public enum PauseState {
        Running, PlayerPaused, EnforcedPause;
    }
}
