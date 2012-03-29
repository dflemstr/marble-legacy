package org.marble.ball;

import java.util.Set;

import jinngine.math.Vector3;
import jinngine.physics.force.Force;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.KeyReleasedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.marble.entity.Interactive;
import org.marble.util.Direction;
import org.marble.util.JinngineConversion;

/**
 * A player-controlled ball.
 */
public class PlayerBall extends Ball implements Interactive {
    /**
     * Alters the {@code inputImpulse} on activation.
     */
    private class AddInputForce implements TriggerAction {
        private final Vector3 addedForce = new Vector3();

        public AddInputForce(final Direction direction, final double magnitude) {
            JinngineConversion.fromVector3(direction.getPhysicalDirection(),
                    addedForce);
            Vector3.multiply(addedForce, magnitude);
        }

        @Override
        public void perform(final Canvas source,
                final TwoInputStates inputStates, final double tpf) {
            Vector3.add(inputForceStrength, addedForce);
        }

    }

    /**
     * An action that applies the {@code inputImpulse} impulse every tick.
     */
    private class BallInputForce implements Force {
        private final Vector3 zero = new Vector3();

        @Override
        public void apply(final double dt) {
            physicalSphere.applyForce(zero, inputForceStrength, dt);
        }
    }

    private final ImmutableSet<InputTrigger> triggers;

    private final AddInputForce forceNorth, forceEast, forceSouth, forceWest;
    private final Force inputForce = new BallInputForce();

    private final Vector3 inputForceStrength = new Vector3();

    /**
     * Creates a new player-controlled ball.
     * 
     * @param radius
     *            The radius of the ball.
     * @param mass
     *            The base mass of the ball.
     */
    public PlayerBall(final BallKind kind, final double radius,
            final Optional<Double> mass) {
        super(kind, radius, mass);

        forceNorth = new AddInputForce(Direction.NORTH, 1);
        forceEast = new AddInputForce(Direction.EAST, 1);
        forceSouth = new AddInputForce(Direction.SOUTH, 1);
        forceWest = new AddInputForce(Direction.WEST, 1);

        final InputTrigger upPressTrigger =
                new InputTrigger(new KeyPressedCondition(Key.UP), forceNorth);
        final InputTrigger leftPressTrigger =
                new InputTrigger(new KeyPressedCondition(Key.LEFT), forceWest);
        final InputTrigger downPressTrigger =
                new InputTrigger(new KeyPressedCondition(Key.DOWN), forceSouth);
        final InputTrigger rightPressTrigger =
                new InputTrigger(new KeyPressedCondition(Key.RIGHT), forceEast);
        final InputTrigger upReleaseTrigger =
                new InputTrigger(new KeyReleasedCondition(Key.UP), forceSouth);
        final InputTrigger leftReleaseTrigger =
                new InputTrigger(new KeyReleasedCondition(Key.LEFT), forceEast);
        final InputTrigger downReleaseTrigger =
                new InputTrigger(new KeyReleasedCondition(Key.DOWN), forceNorth);
        final InputTrigger rightReleaseTrigger =
                new InputTrigger(new KeyReleasedCondition(Key.RIGHT), forceWest);

        triggers =
                ImmutableSet.of(leftPressTrigger, rightPressTrigger,
                        downPressTrigger, upPressTrigger, leftReleaseTrigger,
                        rightReleaseTrigger, downReleaseTrigger,
                        upReleaseTrigger);
    }

    /**
     * Creates a new player-controlled ball.
     * 
     * @param radius
     *            The radius of the ball.
     */
    public PlayerBall(final String kind, final double radius) {
        this(BallKind.valueOf(kind), radius, Optional.<Double> absent());
    }

    /**
     * Creates a new player-controlled ball.
     * 
     * @param radius
     *            The radius of the ball.
     * @param mass
     *            The base mass of the ball.
     */
    public PlayerBall(final String kind, final double radius, final double mass) {
        this(BallKind.valueOf(kind), radius, Optional.of(mass));
    }

    @Override
    public Set<Force> getForces() {
        return Sets.union(ImmutableSet.of(inputForce), super.getForces());
    }

    @Override
    public ImmutableSet<InputTrigger> getTriggers() {
        return triggers;
    }
}
