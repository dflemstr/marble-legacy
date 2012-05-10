package org.marble.session;

import com.jme3.math.Vector3f;

import org.marble.ball.BallKind;

public interface GameSessionListener {
    public void changedLives(int lives);

    public void changedPauseState(GameSession.PauseState pauseState);

    public void changedPoints(int points);

    public void changedRespawnKind(BallKind respawnKind);

    public void changedRespawnPoint(Vector3f respawnPoint);
}
