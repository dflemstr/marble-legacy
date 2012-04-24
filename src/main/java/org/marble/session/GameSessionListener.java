package org.marble.session;

import com.jme3.math.Vector3f;

public interface GameSessionListener {
    public void changedLives(int lives);

    public void changedPoints(int points);

    public void changedPaused(GameSession.PauseState paused);

    public void changedRespawnPoint(Vector3f respawnPoint);
}
