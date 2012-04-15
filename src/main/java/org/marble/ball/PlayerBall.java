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
    private final Vector3f appliedForce = new Vector3f();
    private final Vector3f internalForce = new Vector3f();
    private final Vector3f addedForce = new Vector3f();
    private static final float FORCE_MAGNITUDE = 24.0f;

    public PlayerBall(final BallKind kind) {
        this(kind, 0.5f);
    }

    public PlayerBall(final String kind) {
        this(BallKind.valueOf(kind), 0.5f);
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
    public Set<PlayerInput> handledInputs() {
        return ImmutableSet.of(PlayerInput.MoveBackward,
                PlayerInput.MoveForward, PlayerInput.MoveLeft,
                PlayerInput.MoveRight);
    }

    @Override
    public void handleInput(final PlayerInput input, final boolean isActive) {
        Direction direction;
        switch (input) {
        case MoveForward:
            direction = Direction.North;
            break;
        case MoveBackward:
            direction = Direction.South;
            break;
        case MoveLeft:
            direction = Direction.West;
            break;
        case MoveRight:
            direction = Direction.East;
            break;
        default:
            throw new UnsupportedOperationException(
                    "Cannot handle this input: " + input);
        }
        addedForce.set(direction.getPhysicalDirection());
        addedForce.multLocal(FORCE_MAGNITUDE);
        if (isActive) {
            internalForce.addLocal(addedForce);
        } else {
            internalForce.subtractLocal(addedForce);
        }
    }

    @Override
    public void performActions(final float timePerFrame) {
        appliedForce.set(internalForce);
        appliedForce.multLocal(getBallKind().getMass()
                / getBallKind().getStability());
        getBody().applyCentralForce(appliedForce);
    }
}
