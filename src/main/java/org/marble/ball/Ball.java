package org.marble.ball;

import java.util.concurrent.Callable;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Collidable;
import org.marble.entity.physical.Physical;
import org.marble.frp.FRPUtils;
import org.marble.frp.Reactive;
import org.marble.graphics.EnvironmentNode;
import org.marble.graphics.GeoSphere;
import org.marble.util.Physics;
import org.marble.util.QualityToInteger;

/**
 * A physical ball that can have different materials and physical properties.
 */
public class Ball extends AbstractEntity implements Graphical, Physical,
        Collidable {
    private final class GetEnvironment implements Callable<EnvironmentNode> {
        public boolean wasCalled = false;

        @Override
        public EnvironmentNode call() throws Exception {
            enableEnvironment();
            wasCalled = true;
            return environmentNode.get();
        }
    }

    private static final float MERCURY_REDUCTION_RPS = 1f / 40f;

    private BallKind kind;
    private final float radius;
    private float currentRadius;

    private GeoSphere geometricalBall;

    private RigidBodyControl physicalBall;
    private Spatial graphicalBall;

    private Optional<EnvironmentNode> environmentNode = Optional.absent();

    // Our scene's root node
    private Spatial rootNode;

    private RenderManager renderManager;
    private AssetManager assetManager;

    // How large (2^n) we will let our generated textures be.
    private Reactive<Integer> textureSizeMagnitude;

    private final GetEnvironment getEnvironment = new GetEnvironment();

    /**
     * Creates a new ball.
     */
    public Ball(final BallKind kind) {
        this(kind, 0.5f);
    }

    /**
     * Creates a new ball.
     * 
     * @param radius
     *            The radius.
     */
    public Ball(final BallKind kind, final float radius) {
        this.kind = kind;
        this.radius = radius;
        currentRadius = radius;
    }

    /**
     * Creates a new ball.
     */
    public Ball(final String kind) {
        this(BallKind.valueOf(kind));
    }

    /**
     * Creates a new ball.
     * 
     * @param radius
     *            The radius.
     */
    public Ball(final String kind, final float radius) {
        this(BallKind.valueOf(kind), radius);
    }

    public BallKind getBallKind() {
        return kind;
    }

    @Override
    public RigidBodyControl getBody() {
        return physicalBall;
    }

    @Override
    public void initialize(final Game game) throws Exception {
        super.initialize(game);
        rootNode = game.getGraphicsEngine().getRootNode();
        renderManager = game.getGraphicsEngine().getRenderManager();
        assetManager = game.getAssetManager();

        // The lowest texture setting makes textures be 16x16; the size is
        // doubled for each step
        textureSizeMagnitude =
                FRPUtils.map(game.getSettings().environmentQuality,
                        new QualityToInteger());

        geometricalBall =
                new GeoSphere(true, radius, 4, GeoSphere.TextureMode.Projected);

        graphicalBall = new Geometry("ball", geometricalBall);
        getSpatial().attachChild(graphicalBall);

        physicalBall =
                new RigidBodyControl(new SphereCollisionShape(radius),
                        kind.getMass());
        physicalBall.activate();
        physicalBall.setSleepingThresholds(0, 0);
        getSpatial().addControl(physicalBall);

        setBallKind(kind);
    }

    private void enableEnvironment() {
        if (!environmentNode.isPresent()) {
            final EnvironmentNode node =
                    new EnvironmentNode(rootNode, renderManager,
                            textureSizeMagnitude);
            environmentNode = Optional.of(node);
            getSpatial().attachChild(node);
        }
    }

    private void disableEnvironment() {
        if (environmentNode.isPresent()) {
            getSpatial().detachChild(environmentNode.get());
            environmentNode.get().destroy();
            environmentNode = Optional.absent();
        }
    }

    /**
     * Changes the kind of ball that this is.
     * 
     * @param kind
     *            The kind of ball to switch to.
     * @throws Exception
     */
    public void setBallKind(final BallKind kind) throws Exception {
        this.kind = kind;
        getEnvironment.wasCalled = false;
        graphicalBall.setMaterial(kind.createMaterial(assetManager,
                getEnvironment));

        if (!getEnvironment.wasCalled) {
            disableEnvironment();
        }

        physicalBall.setMass(kind.getMass());
        physicalBall.setLinearDamping(kind.getLinearDamping());
        physicalBall.setGravity(Physics.GRAVITY);
        // physicalBall.setDamping(kind.getLinearDamping(),
        // kind.getAngularDamping());
        // physicalBall.setFriction(kind.getFriction());
        // physicalBall.setRestitution(kind.getRestitution());
    }

    @Override
    public void update(final float timePerFrame) {
        if (kind == BallKind.Mercury) {
            currentRadius -= timePerFrame * MERCURY_REDUCTION_RPS;
            if (currentRadius <= 0) {
                die();
            } else {
                final float scale = currentRadius / radius;
                final TempVars vars = TempVars.get();
                vars.vect1.set(scale, scale, scale);
                physicalBall.getCollisionShape().setScale(vars.vect1);
                vars.release();
                getSpatial().setLocalScale(scale);
            }
        }
    }

    @Override
    public void destroy() {
        disableEnvironment();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName())
                .add("kind", kind).add("radius", radius).toString();
    }

    @Override
    public void handleCollisionWith(final Physical other,
            final PhysicsCollisionEvent event) {
        // TODO die if glass and too strong impulse
    }

    @Override
    public void die() {
        game.removeEntity(this);
    }

    protected void reset() {
        getBody().setPhysicsRotation(Quaternion.IDENTITY);
        getBody().setAngularVelocity(Vector3f.ZERO);
        getBody().setLinearVelocity(Vector3f.ZERO);
        getBody().getCollisionShape().setScale(Vector3f.UNIT_XYZ);
        getSpatial().setLocalRotation(Quaternion.IDENTITY);
        getSpatial().setLocalScale(1);
        currentRadius = radius;
    }
}
