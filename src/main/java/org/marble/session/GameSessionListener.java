package org.marble.session;

import com.jme3.math.Vector3f;

import org.marble.ball.BallKind;

public interface GameSessionListener {
    public void changedLives(int lives);

    public void changedPoints(int points);

    public void changedPauseState(GameSession.PauseState pauseState);

    public void changedRespawnPoint(Vector3f respawnPoint);

    public void changedRespawnKind(BallKind respawnKind);
}
