package org.marble.ball;

import java.util.Set;

import javax.vecmath.Vector3f;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.KeyReleasedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;

import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.linearmath.IDebugDraw;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.marble.entity.Interactive;
import org.marble.util.Direction;

/**
 * A player-controlled ball.
 */
public class PlayerBall extends Ball implements Interactive {
    /**
     * Alters the {@code inputImpulse} on activation.
     */
    private class AddInputImpulse implements TriggerAction {
        private final Vector3f addedImpulse = new Vector3f();

        public AddInputImpulse(final Direction direction, final double magnitude) {
            addedImpulse.set(direction.getPhysicalDirection());
            addedImpulse.scale((float) magnitude);
        }

        @Override
        public void perform(final Canvas source,
                final TwoInputStates inputStates, final double tpf) {
            inputImpulse.add(addedImpulse);
        }

    }

    /**
     * An action that applies the {@code inputImpulse} impulse every tick.
     */
    private class PushBallAction extends ActionInterface {
        @Override
        public void debugDraw(final IDebugDraw debugDrawer) {
            // Do nothing
        }

        @Override
        public void updateAction(final CollisionWorld collisionWorld,
                final float deltaTimeStep) {
            PlayerBall.this.physicalSphere.activate();
            PlayerBall.this.physicalSphere.applyCentralImpulse(inputImpulse);
        }

    }

    private final ImmutableSet<InputTrigger> triggers;

    private final AddInputImpulse forceNorth, forceEast, forceSouth, forceWest;
    private final PushBallAction pushBallAction = new PushBallAction();
    private final Vector3f inputImpulse = new Vector3f();

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

        forceNorth = new AddInputImpulse(Direction.NORTH, 1);
        forceEast = new AddInputImpulse(Direction.EAST, 1);
        forceSouth = new AddInputImpulse(Direction.SOUTH, 1);
        forceWest = new AddInputImpulse(Direction.WEST, 1);

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
    public Set<ActionInterface> getActions() {
        return Sets.union(ImmutableSet.of(pushBallAction), super.getActions());
    }

    @Override
    public Set<InputTrigger> getTriggers() {
        return triggers;
    }
}
