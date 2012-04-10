package org.marble.ball;

import java.util.Set;

import javax.vecmath.Vector3f;

import com.ardor3d.image.Texture;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.GeoSphere;
import com.ardor3d.scenegraph.shape.GeoSphere.TextureMode;
import com.ardor3d.util.TextureManager;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.Collidable;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;
import org.marble.graphics.ChromaticAberrationNode;
import org.marble.graphics.ReflectionNode;
import org.marble.graphics.shader.LightDataLogic;
import org.marble.util.Shaders;

/**
 * A physical ball that can have different materials and physical properties.
 */
public class Ball extends AbstractEntity implements Graphical, Physical,
        Collidable {
    protected BallKind kind;
    protected final double radius;

    // The actual node that is transformed with our entity and body
    protected final Node centerNode;

    // A node whose sub-nodes should make out the actual ball part of the
    // geometrical nodes. The sub-nodes will be changed and/or added/removed
    // when the ball material changes.
    protected final Node ballNode;

    protected final RigidBody physicalSphere;

    // Our scene's root node
    private Spatial rootNode;

    // How large (2^n) we will let our generated textures be.
    private int textureSizeMagnitude;

    private final TextureState ts = new TextureState();
    private final BlendState bs = new BlendState();

    /**
     * Creates a new ball.
     * 
     * @param radius
     *            The radius.
     * @param mass
     *            The base mass.
     */
    public Ball(final BallKind kind, final double radius,
            final Optional<Double> mass) {
        this.kind = kind;
        this.radius = radius;

        centerNode = new Node();

        ballNode = new Node();
        ballNode.getSceneHints().setRenderBucketType(
                RenderBucketType.Transparent);
        centerNode.attachChild(ballNode);

        bs.setEnabled(true);
        bs.setBlendEnabled(true);

        final CollisionShape geometricalSphere =
                new SphereShape((float) radius);
        final Vector3f inertia = new Vector3f(0, 0, 0);
        geometricalSphere.calculateLocalInertia((float) (double) mass.or(0.0),
                inertia);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo((float) (double) mass.or(0.0),
                        new DefaultMotionState(), geometricalSphere, inertia);
        physicalSphere = new RigidBody(info);
    }

    /**
     * Creates a new ball.
     * 
     * @param radius
     *            The radius.
     */
    public Ball(final String kind, final double radius) {
        this(BallKind.valueOf(kind), radius, Optional.<Double> absent());
    }

    /**
     * Creates a new ball.
     * 
     * @param radius
     *            The radius.
     * @param mass
     *            The base mass.
     */
    public Ball(final String kind, final double radius, final double mass) {
        this(BallKind.valueOf(kind), radius, Optional.of(mass));
    }

    @Override
    public RigidBody getBody() {
        return physicalSphere;
    }

    @Override
    public Spatial getSpatial() {
        return centerNode;
    }

    @Override
    public void initialize(final Game game) {
        rootNode = game.getGraphicsEngine().getRootNode();

        // The lowest texture setting makes textures be 16x16; the size is
        // doubled for each step
        textureSizeMagnitude =
                game.getSettings().environmentQuality.getValue().ordinal() + 4;

        setBallKind(kind);
    }

    /**
     * Changes the kind of ball that this is.
     * 
     * @param kind
     *            The kind of ball to switch to.
     */
    public void setBallKind(final BallKind kind) {
        // TODO implement material properties and changes.
        ballNode.detachAllChildren();
        final GeoSphere sphere =
                new GeoSphere("ball", true, radius, 4, TextureMode.Projected);
        switch (kind) {
        case Wood:
            final GLSLShaderObjectsState wood = Shaders.loadShader("wood");
            wood.setShaderDataLogic(new LightDataLogic());
            wood.setUniform("woodGradient", 0);

            final Vector3 vec = Vector3.fetchTempInstance();

            // The trunkCenter vectors define a line that is the center of the
            // trunk that our wood was cut from - all the "rings" will be around
            // this axis.
            randomize(vec);
            wood.setUniform("trunkCenter1", vec);

            randomize(vec);
            wood.setUniform("trunkCenter2", vec);

            // The noiseSeed vector seeds the random noise generator. The
            // generator has a period of 289.
            randomize(vec);
            vec.multiplyLocal(289);
            wood.setUniform("noiseSeed", vec);

            // The variation is a value between 0.0 and 1.0 that determines
            // which column of the wood gradient texture that is used for
            // tinting the material.
            wood.setUniform("variation", (float) Math.random());

            Vector3.releaseTempInstance(vec);
            sphere.setRenderState(wood);

            ts.setTexture(TextureManager.load("wood-gradient.png",
                    Texture.MinificationFilter.BilinearNoMipMaps, false));
            sphere.setRenderState(ts);
            ballNode.attachChild(sphere);
            break;
        case Stone:
            ts.setTexture(TextureManager.load("stone.png",
                    Texture.MinificationFilter.Trilinear, true));
            sphere.setRenderState(ts);
            ballNode.attachChild(sphere);
            break;
        case Fabric:
            ts.setTexture(TextureManager.load("fabric.png",
                    Texture.MinificationFilter.Trilinear, true));
            sphere.setRenderState(ts);
            sphere.setRenderState(bs);
            ballNode.attachChild(sphere);
            break;
        case Glass:
            final Node chromAberrator =
                    new ChromaticAberrationNode(rootNode, new ColorRGBA(0.941f,
                            0.984f, 1, 1), textureSizeMagnitude);
            chromAberrator.attachChild(sphere);
            ballNode.attachChild(chromAberrator);
            break;
        case Mercury:
            final Node reflector =
                    new ReflectionNode(rootNode, new ColorRGBA(0.941f, 0.984f,
                            1, 1), textureSizeMagnitude);
            reflector.attachChild(sphere);
            ballNode.attachChild(reflector);
            break;
        }
        ballNode.updateGeometricState(0);
        this.kind = kind;
    }

    public BallKind getBallKind() {
        return kind;
    }

    private void randomize(final Vector3 vec) {
        vec.setX(Math.random());
        vec.setY(Math.random());
        vec.setZ(Math.random());
    }

    @Override
    public Set<ActionInterface> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public void handleContactAdded(final Physical other) {
    }

    @Override
    public void handleContactRemoved(final Physical other) {
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", name).add("kind", kind)
                .add("radius", radius).toString();
    }
}
