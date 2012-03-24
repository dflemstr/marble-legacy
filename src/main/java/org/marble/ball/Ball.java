package org.marble.ball;

import java.util.Set;

import javax.vecmath.Vector3f;

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

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;

import com.google.common.collect.ImmutableSet;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;
import org.marble.graphics.ChromaticAberrationNode;
import org.marble.graphics.EntityController;
import org.marble.graphics.ReflectionNode;
import org.marble.physics.EntityMotionState;

/**
 * A physical ball that can have different materials and physical properties.
 */
public class Ball extends AbstractEntity implements Graphical, Physical {
    protected final float radius;
    protected final float mass;
    protected BallKind kind;

    protected final Node centerNode;
    protected final Node ballNode;
    protected RigidBody physicalSphere;
    private Spatial rootNode;

    private GeoSphere sphere;
    private final TextureState ts = new TextureState();
    private final BlendState bs = new BlendState();

    /**
     * Creates a new ball.
     *
     * @param name
     *            The name, for debug purposes.
     * @param transform
     *            The local transform, including translation, rotation and
     *            scale.
     * @param radius
     *            The radius.
     * @param mass
     *            The base mass.
     */
    public Ball(final BallKind kind, final float radius, final float mass) {
        this.radius = radius;
        this.mass = mass;
        this.kind = kind;

        centerNode = new Node();
        centerNode.addController(new EntityController(this));

        ballNode = new Node();
        ballNode.getSceneHints().setRenderBucketType(
                RenderBucketType.Transparent);
        centerNode.attachChild(ballNode);

        bs.setEnabled(true);
        bs.setBlendEnabled(true);
        bs.setReference(0.7f);
    }

    public Ball(final String kind, final float radius, final float mass) {
        this(BallKind.valueOf(kind), radius, mass);
    }

    @Override
    public Set<ActionInterface> getActions() {
        return ImmutableSet.of();
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

        final CollisionShape physicalShape = new SphereShape(radius);
        final Vector3f inertia = new Vector3f(0, 0, 0);
        physicalShape.calculateLocalInertia(mass, inertia);

        final MotionState motionState = new EntityMotionState(this);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo(mass, motionState, physicalShape,
                        inertia);
        physicalSphere = new RigidBody(info);

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
        sphere = new GeoSphere("ball", true, radius, 4, TextureMode.Projected);
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
