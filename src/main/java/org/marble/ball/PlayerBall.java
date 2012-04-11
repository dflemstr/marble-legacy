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
     * Alters the {@code inputForce} on activation.
     */
    private class AddInputForce implements TriggerAction {
        private final Vector3f addedForce = new Vector3f();

        public AddInputForce(final Direction direction, final double magnitude) {
            addedForce.set(direction.getPhysicalDirection());
            addedForce.scale((float) magnitude);
        }

        @Override
        public void perform(final Canvas source,
                final TwoInputStates inputStates, final double tpf) {
            inputForce.add(addedForce);
        }

    }

    /**
     * An action that applies the {@code inputForce} force every tick.
     */
    private class PushBallAction extends ActionInterface {

        private final Vector3f appliedForce = new Vector3f();

        @Override
        public void debugDraw(final IDebugDraw debugDrawer) {
            // Do nothing
        }

        @Override
        public void updateAction(final CollisionWorld collisionWorld,
                final float deltaTimeStep) {
            PlayerBall.this.physicalSphere.activate();
            appliedForce.set(inputForce);
            appliedForce.scale((float) (kind.getMass() / kind
                    .getStability()));
            PlayerBall.this.physicalSphere.applyCentralForce(appliedForce);
        }
    }

    private static final double FORCE_MAGNITUDE = 20.0;

    private final ImmutableSet<InputTrigger> triggers;

    private final AddInputForce forceNorth, forceEast, forceSouth, forceWest;
    private final PushBallAction pushBallAction = new PushBallAction();
    private final Vector3f inputForce = new Vector3f();

    /**
     * Creates a new player-controlled ball.
     * 
     * @param radius
     *            The radius of the ball.
     * @param mass
     *            The base mass of the ball.
     */
    public PlayerBall(final BallKind kind, final double radius) {
        super(kind, radius);

        forceNorth = new AddInputForce(Direction.NORTH, FORCE_MAGNITUDE);
        forceEast = new AddInputForce(Direction.EAST, FORCE_MAGNITUDE);
        forceSouth = new AddInputForce(Direction.SOUTH, FORCE_MAGNITUDE);
        forceWest = new AddInputForce(Direction.WEST, FORCE_MAGNITUDE);

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
        this(BallKind.valueOf(kind), radius);
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
