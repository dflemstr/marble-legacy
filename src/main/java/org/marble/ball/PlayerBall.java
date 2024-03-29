package org.marble.ball;

import java.util.Set;

import com.jme3.math.Vector3f;

import com.google.common.collect.ImmutableSet;

import org.marble.entity.interactive.Interactive;
import org.marble.entity.physical.Actor;
import org.marble.input.PlayerInput;
import org.marble.util.Direction;

/**
 * A player-controlled ball.
 */
public class PlayerBall extends Ball implements Interactive, Actor {
    private final Vector3f addedForce = new Vector3f();
    private final Vector3f appliedForce = new Vector3f();
    private boolean goingWest, goingEast, goingNorth, goingSouth;
    private final Vector3f internalForce = new Vector3f();

    public PlayerBall(final BallKind kind) {
        this(kind, 0.5f);
    }

    /**
     * Creates a new player-controlled ball.
     * 
     * @param radius
     *            The radius of the ball.
     * @param mass
     *            The base mass of the ball.
     */
    public PlayerBall(final BallKind kind, final float radius) {
        super(kind, radius);
    }

    public PlayerBall(final String kind) {
        this(BallKind.valueOf(kind), 0.5f);
    }

    /**
     * Creates a new player-controlled ball.
     * 
     * @param radius
     *            The radius of the ball.
     */
    public PlayerBall(final String kind, final float radius) {
        this(BallKind.valueOf(kind), radius);
    }

    @Override
    public void die() {
        game.killBall();
    }

    @Override
    public Set<PlayerInput> handledInputs() {
        return ImmutableSet.of(PlayerInput.MoveBackward,
                PlayerInput.MoveForward, PlayerInput.MoveLeft,
                PlayerInput.MoveRight);
    }

    @Override
    public void handleInput(final PlayerInput input, final boolean isActive) {
        final Direction direction;
        final boolean alreadyActive;
        switch (input) {
        case MoveForward:
            alreadyActive = goingNorth;
            goingNorth = isActive;
            direction = Direction.North;
            break;
        case MoveBackward:
            alreadyActive = goingSouth;
            goingSouth = isActive;
            direction = Direction.South;
            break;
        case MoveLeft:
            alreadyActive = goingWest;
            goingWest = isActive;
            direction = Direction.West;
            break;
        case MoveRight:
            alreadyActive = goingEast;
            goingEast = isActive;
            direction = Direction.East;
            break;
        default:
            throw new UnsupportedOperationException(
                    "Cannot handle this input: " + input);
        }
        addedForce.set(direction.getPhysicalDirection());
        if (isActive && !alreadyActive) {
            internalForce.addLocal(addedForce);
        } else if (alreadyActive) {
            internalForce.subtractLocal(addedForce);
        }
    }

    @Override
    public void performActions(final float timePerFrame) {
        appliedForce.set(internalForce);
        appliedForce.multLocal(getBallKind().getForce());
        getBody().applyCentralForce(appliedForce);
    }

    public void resetMoveTo(final Vector3f respawnPoint) {
        getBody().setPhysicsLocation(respawnPoint);
        getSpatial().setLocalTranslation(respawnPoint);
        reset();
    }
}
