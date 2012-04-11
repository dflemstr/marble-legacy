package org.marble.block;

import java.util.Map;
import java.util.logging.Logger;

import javax.vecmath.Vector3f;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Box;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import org.marble.ball.Ball;
import org.marble.ball.BallKind;
import org.marble.entity.AbstractEntity;
import org.marble.entity.Collidable;
import org.marble.entity.Connected;
import org.marble.entity.Connector;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;

public class Transformer extends AbstractEntity implements Physical, Graphical,
        Connected, Collidable {
    private static final Logger log = Logger.getLogger(AbstractEntity.class
            .getName());
    private final BallKind targetKind;
    private final Box graphicalBox;
    private final RigidBody physicalBox;

    public Transformer(final String targetKind) {
        this(BallKind.valueOf(targetKind));
    }

    public Transformer(final BallKind targetKind) {
        this.targetKind = targetKind;

        graphicalBox = new Box("slab", Vector3.ZERO, 0.5, 0.5, 0.5);

        final CollisionShape geometricalBox =
                new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));

        final Vector3f inertia = new Vector3f(0, 0, 0);
        geometricalBox.calculateLocalInertia(0.0f, inertia);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo(0.0f, new DefaultMotionState(),
                        geometricalBox, inertia);
        physicalBox = new RigidBody(info);
    }

    @Override
    public void handleContactAdded(final Physical other) {
        if (other instanceof Ball) {
            final Ball ball = (Ball) other;
            if (ball.getBallKind() != targetKind) {
                ball.setBallKind(targetKind);
                log.info("Transformed " + ball + " into material " + targetKind);
            }
        }
    }

    @Override
    public void handleContactRemoved(final Physical other) {
        // Do nothing
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return ImmutableMap.of();
    }

    @Override
    public Spatial getSpatial() {
        return graphicalBox;
    }

    @Override
    public RigidBody getBody() {
        return physicalBox;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", name)
                .add("targetKind", targetKind).toString();
    }
}
