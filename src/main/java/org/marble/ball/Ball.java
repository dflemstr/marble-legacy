package org.marble.ball;

import java.util.Set;

import jinngine.physics.Body;
import jinngine.physics.force.Force;

import com.ardor3d.image.Texture;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.GeoSphere;
import com.ardor3d.scenegraph.shape.GeoSphere.TextureMode;
import com.ardor3d.util.TextureManager;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;
import org.marble.graphics.ChromaticAberrationNode;
import org.marble.graphics.ReflectionNode;
import org.marble.physics.GravitationalForce;

/**
 * A physical ball that can have different materials and physical properties.
 */
public class Ball extends AbstractEntity implements Graphical, Physical {
    protected BallKind kind;
    protected final double radius;

    protected final Node centerNode;
    protected final Node ballNode;
    protected final Body physicalSphere;
    protected final Force gravityForce;
    private Spatial rootNode;

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
        bs.setReference(0.7f);

        final jinngine.geometry.Sphere geometricalSphere =
                new jinngine.geometry.Sphere(radius);

        physicalSphere = new Body("ball", geometricalSphere);

        if (mass.isPresent()) {
            geometricalSphere.setMass(mass.get());
        } else {
            physicalSphere.setFixed(true);
        }

        gravityForce = new GravitationalForce(physicalSphere);
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
    public Body getBody() {
        return physicalSphere;
    }

    @Override
    public Set<Force> getForces() {
        return ImmutableSet.of(gravityForce);
    }

    @Override
    public Spatial getSpatial() {
        return centerNode;
    }

    @Override
    public void initialize(final Game game) {
        rootNode = game.getGraphicsEngine().getRootNode();

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
            ts.setTexture(TextureManager.load("wood.png",
                    Texture.MinificationFilter.Trilinear, true));
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
                            0.984f, 1, 1));
            chromAberrator.attachChild(sphere);
            ballNode.attachChild(chromAberrator);
            break;
        case Mercury:
            final Node reflector =
                    new ReflectionNode(rootNode, new ColorRGBA(0.941f, 0.984f,
                            1, 1));
            reflector.attachChild(sphere);
            ballNode.attachChild(reflector);
            break;
        }
        this.kind = kind;
    }
}
