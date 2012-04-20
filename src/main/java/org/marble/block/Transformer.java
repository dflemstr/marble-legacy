package org.marble.block;

import java.util.Map;
import java.util.logging.Logger;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import org.marble.Game;
import org.marble.ball.Ball;
import org.marble.ball.BallKind;
import org.marble.entity.AbstractEntity;
import org.marble.entity.connected.Connected;
import org.marble.entity.connected.Connector;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Collidable;
import org.marble.entity.physical.Physical;

public class Transformer extends AbstractEntity implements Physical, Graphical,
        Connected, Collidable {
    private static final Logger log = Logger.getLogger(AbstractEntity.class
            .getName());
    private final BallKind targetKind;
    private Geometry graphicalBox;
    private RigidBodyControl physicalBox;

    public Transformer(final BallKind targetKind) {
        this.targetKind = targetKind;
    }

    public Transformer(final String targetKind) {
        this(BallKind.valueOf(targetKind));
    }

    @Override
    public void initialize(final Game game) {
        final AssetManager assetManager = game.getAssetManager();

        graphicalBox =
                new Geometry("slab", new Box(Vector3f.ZERO, 0.5f, 0.5f, 0.5f));
        graphicalBox.setMaterial(assetManager
                .loadMaterial("Materials/Misc/Undefined.j3m"));
        getSpatial().attachChild(graphicalBox);

        physicalBox =
                new RigidBodyControl(new BoxCollisionShape(new Vector3f(0.5f,
                        0.5f, 0.5f)), 0);
        getSpatial().addControl(physicalBox);
    }

    @Override
    public RigidBodyControl getBody() {
        return physicalBox;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return ImmutableMap.of();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName())
                .add("targetKind", targetKind).toString();
    }

    @Override
    public void handleCollisionWith(final Physical other,
            final PhysicsCollisionEvent event) {

        if (other instanceof Ball) {
            final Ball ball = (Ball) other;
            if (ball.getBallKind() != targetKind) {
                ball.setBallKind(targetKind);
                log.info("Transformed " + ball + " into material " + targetKind);
            }
        }
    }
}
