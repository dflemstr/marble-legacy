package org.marble.ball;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.texture.TextureCubeMap;
import com.jme3.util.TempVars;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Collidable;
import org.marble.entity.physical.Physical;
import org.marble.frp.FRPUtils;
import org.marble.frp.Reactive;
import org.marble.frp.ReactiveListener;
import org.marble.graphics.EnvironmentNode;
import org.marble.graphics.GeoSphere;
import org.marble.graphics.MaterialSP;
import org.marble.util.Quality;

/**
 * A physical ball that can have different materials and physical properties.
 */
public class Ball extends AbstractEntity implements Graphical, Physical,
        Collidable {
    private static final float MERCURY_REDUCTION_RPS = 1f / 40f;

    private static final class QualityToInteger implements
            Function<Quality, Integer> {
        @Override
        public Integer apply(final Quality input) {
            return input.ordinal() + 4;
        }
    }

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
    public void initialize(final Game game) {
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
     */
    public void setBallKind(final BallKind kind) {
        // TODO implement material properties and changes.

        final Material material;
        switch (kind) {
        case Wood:
            disableEnvironment();
            material =
                    new MaterialSP(
                            assetManager
                                    .loadMaterial("Materials/Organic/Wood.j3m"));

            final Vector3f vec = new Vector3f();

            // The trunkCenter vectors define a line that is the center of the
            // trunk that our wood was cut from - all the "rings" will be around
            // this axis.
            randomize(vec);
            // material.setVector3("TrunkCenter1", vec);

            randomize(vec);
            // material.setVector3("TrunkCenter2", vec);

            // The noiseSeed vector seeds the random noise generator. The
            // generator has a period of 289.
            randomize(vec);
            vec.multLocal(289);
            material.setVector3("NoiseSeed", vec);

            // The variation is a value between 0.0 and 1.0 that determines
            // which column of the wood gradient texture that is used for
            // tinting the material.
            material.setFloat("Variation", (float) Math.random());
            break;
        case Stone:
            disableEnvironment();
            material = assetManager.loadMaterial("Materials/Mineral/Stone.j3m");
            break;
        case Fabric:
            disableEnvironment();
            material =
                    assetManager.loadMaterial("Materials/Organic/Fabric.j3m");
            material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            break;
        case Glass:
            enableEnvironment();
            material =
                    new MaterialSP(
                            assetManager
                                    .loadMaterial("Materials/Mineral/Glass.j3m"));
            FRPUtils.addAndCallReactiveListener(environmentNode.get()
                    .getEnvironment(), new ReactiveListener<TextureCubeMap>() {

                @Override
                public void valueChanged(final TextureCubeMap value) {
                    material.setTexture("EnvironmentMap", value);
                }
            });
            break;
        case Mercury:
            enableEnvironment();
            material =
                    new MaterialSP(
                            assetManager
                                    .loadMaterial("Materials/Metal/Mercury.j3m"));
            FRPUtils.addAndCallReactiveListener(environmentNode.get()
                    .getEnvironment(), new ReactiveListener<TextureCubeMap>() {

                @Override
                public void valueChanged(final TextureCubeMap value) {
                    material.setTexture("EnvironmentMap", value);
                }
            });
            break;
        default:
            throw new UnsupportedOperationException(
                    "Unimplemented ball material for kind " + kind);
        }
        graphicalBall.setMaterial(material);

        physicalBall.setMass(kind.getMass());
        // physicalBall.setDamping(kind.getLinearDamping(),
        // kind.getAngularDamping());
        physicalBall.setFriction(kind.getFriction());
        physicalBall.setRestitution(kind.getRestitution());

        this.kind = kind;
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

    private void randomize(final Vector3f vec) {
        vec.setX((float) Math.random());
        vec.setY((float) Math.random());
        vec.setZ((float) Math.random());
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
