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
    private class DisplaceImpulse implements TriggerAction {
        private final Vector3f impulse;

        public DisplaceImpulse(final Direction direction, final float magnitude) {
            impulse = new Vector3f(direction.getPhysicalDirection());
            impulse.scale(magnitude);
        }

        @Override
        public void perform(final Canvas source,
                final TwoInputStates inputStates, final double tpf) {
            inputImpulse.add(impulse);
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

    private final DisplaceImpulse disImpNorth, disImpEast, disImpSouth,
            disImpWest;
    private final ActionInterface pushBallAction = new PushBallAction();

    private final Vector3f inputImpulse = new Vector3f();

    /**
     * Creates a new player-controlled ball.
     *
     * @param name
     *            The name of the ball, for debug purposes.
     * @param transform
     *            The local transform of the ball, including translation,
     *            rotation and scale.
     * @param radius
     *            The radius of the ball.
     * @param mass
     *            The base mass of the ball.
     */
    public PlayerBall(final BallKind kind, final float radius, final float mass) {
        super(kind, radius, mass);
        disImpNorth = new DisplaceImpulse(Direction.NORTH, 1);
        disImpEast = new DisplaceImpulse(Direction.EAST, 1);
        disImpSouth = new DisplaceImpulse(Direction.SOUTH, 1);
        disImpWest = new DisplaceImpulse(Direction.WEST, 1);

        final InputTrigger upPressTrigger =
                new InputTrigger(new KeyPressedCondition(Key.UP), disImpNorth);
        final InputTrigger leftPressTrigger =
                new InputTrigger(new KeyPressedCondition(Key.LEFT), disImpWest);
        final InputTrigger downPressTrigger =
                new InputTrigger(new KeyPressedCondition(Key.DOWN), disImpSouth);
        final InputTrigger rightPressTrigger =
                new InputTrigger(new KeyPressedCondition(Key.RIGHT), disImpEast);
        final InputTrigger upReleaseTrigger =
                new InputTrigger(new KeyReleasedCondition(Key.UP), disImpSouth);
        final InputTrigger leftReleaseTrigger =
                new InputTrigger(new KeyReleasedCondition(Key.LEFT), disImpEast);
        final InputTrigger downReleaseTrigger =
                new InputTrigger(new KeyReleasedCondition(Key.DOWN),
                        disImpNorth);
        final InputTrigger rightReleaseTrigger =
                new InputTrigger(new KeyReleasedCondition(Key.RIGHT),
                        disImpWest);

        triggers =
                ImmutableSet.of(leftPressTrigger, rightPressTrigger,
                        downPressTrigger, upPressTrigger, leftReleaseTrigger,
                        rightReleaseTrigger, downReleaseTrigger,
                        upReleaseTrigger);
    }

    public PlayerBall(final String kind, final float radius, final float mass) {
        this(BallKind.valueOf(kind), radius, mass);
    }

    @Override
    public Set<ActionInterface> getActions() {
        return Sets.union(ImmutableSet.of(pushBallAction), super.getActions());
    }

    @Override
    public ImmutableSet<InputTrigger> getTriggers() {
        return triggers;
    }
}
