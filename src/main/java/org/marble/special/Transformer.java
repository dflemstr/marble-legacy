package org.marble.special;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
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
import org.marble.frp.FRPUtils;
import org.marble.graphics.EnvironmentNode;
import org.marble.graphics.GeoSphere;
import org.marble.util.QualityToInteger;

public class Transformer extends AbstractEntity implements Physical, Graphical,
        Connected, Collidable {
    private final class CreateEnvironmentNode implements
            Callable<EnvironmentNode> {
        private final Game game;

        private CreateEnvironmentNode(final Game game) {
            this.game = game;
        }

        @Override
        public EnvironmentNode call() throws Exception {
            final EnvironmentNode node =
                    new EnvironmentNode(game.getGraphicsEngine().getRootNode(),
                            game.getGraphicsEngine().getRenderManager(),
                            FRPUtils.map(game.getSettings().environmentQuality,
                                    new QualityToInteger()));
            getSpatial().attachChild(node);
            environmentNode = Optional.of(node);
            return node;
        }
    }

    private static final Logger log = Logger.getLogger(AbstractEntity.class
            .getName());
    private final BallKind targetKind;
    private RigidBodyControl physicalBox;
    private Optional<EnvironmentNode> environmentNode = Optional.absent();

    public Transformer(final BallKind targetKind) {
        this.targetKind = targetKind;
    }

    public Transformer(final String targetKind) {
        this(BallKind.valueOf(targetKind));
    }

    @Override
    public void initialize(final Game game) throws Exception {
        final AssetManager assetManager = game.getAssetManager();

        final Geometry graphicalBlock =
                new Geometry("sphere", new GeoSphere(true,
                        3f * FastMath.sqrt(2) / 8f, 1,
                        GeoSphere.TextureMode.Projected));

        final Callable<EnvironmentNode> getEnvironment =
                new CreateEnvironmentNode(game);
        graphicalBlock.setMaterial(targetKind.createMaterial(assetManager,
                getEnvironment));
        getSpatial().attachChild(graphicalBlock);

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
    public void destroy() {
        if (environmentNode.isPresent()) {
            environmentNode.get().destroy();
        }
    }

    @Override
    public void handleCollisionWith(final Physical other,
            final PhysicsCollisionEvent event) {

        if (other instanceof Ball) {
            final Ball ball = (Ball) other;
            try {
                ball.setBallKind(targetKind, true, false);
            } catch (final Exception e) {
                game.handleError(e);
            }
            log.info("Transformed " + ball + " into material " + targetKind);
        }
    }
}
