package org.marble.block;

import java.util.Map;

import javax.vecmath.Vector3f;

import com.ardor3d.image.Texture;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;

import com.google.common.collect.ImmutableSet;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.Connectivity;
import org.marble.entity.Connector;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;
import org.marble.graphics.EntityController;
import org.marble.graphics.SegmentedBox;
import org.marble.physics.EntityMotionState;
import org.marble.util.Connectors;
import org.marble.util.Shaders;

/**
 * A box-shaped block.
 */
public class Slab extends AbstractEntity implements Connectivity, Graphical,
        Physical {
    private final float width, height, depth;
    private final float mass;
    private Box graphicalBox;
    private RigidBody physicalBox;
    private final GLSLShaderObjectsState wood;
    private final Texture woodGradient;
    private final TextureState ts;

    /**
     * Creates a new slab.
     *
     * @param width
     *            The size along the X-axis.
     * @param height
     *            The size along the Y-axis.
     * @param depth
     *            The size along the Z-axis.
     */
    public Slab(final float width, final float height, final float depth) {
        this(width, height, depth, 0.0f);
    }

    /**
     * Creates a new slab.
     *
     * @param width
     *            The size along the X-axis.
     * @param height
     *            The size along the Y-axis.
     * @param depth
     *            The size along the Z-axis.
     * @param mass
     *            The mass.
     */
    public Slab(final float width, final float height, final float depth,
            final float mass) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.mass = mass;

        woodGradient =
                TextureManager.load("wood-gradient.png",
                        Texture.MinificationFilter.BilinearNoMipMaps, false);

        ts = new TextureState();
        ts.setTexture(woodGradient, 0);

        wood = Shaders.loadShader("wood");
        wood.setUniform("woodGradient", 0);

        final Vector3 vec = Vector3.fetchTempInstance();
        randomize(vec);
        vec.multiplyLocal(width / 2, height / 2, depth / 2);
        vec.subtractLocal(width / 4, height / 4, 0);
        wood.setUniform("trunkCenter1", vec);

        randomize(vec);
        vec.multiplyLocal(width / 2, height / 2, depth / 2);
        vec.subtractLocal(width / 4, height / 4, depth / 2);
        wood.setUniform("trunkCenter2", vec);

        randomize(vec);
        vec.multiplyLocal(289);
        wood.setUniform("noiseSeed", vec);

        wood.setUniform("variation", (float) Math.random());

        Vector3.releaseTempInstance(vec);
    }

    @Override
    public ImmutableSet<ActionInterface> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public RigidBody getBody() {
        return physicalBox;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return Connectors.fromBox(width, height, depth);
    }

    @Override
    public Spatial getSpatial() {
        return graphicalBox;
    }

    @Override
    public void initialize(final Game game) {
        graphicalBox =
                new SegmentedBox("slab", 1, 1, 0.3, Vector3.ZERO, width / 2,
                        height / 2, depth / 2);
        graphicalBox.setRenderState(wood);
        graphicalBox.setRenderState(ts);
        graphicalBox.addController(new EntityController(this));

        final CollisionShape physicalShape =
                new BoxShape(new Vector3f(width / 2, height / 2, depth / 2));
        final Vector3f inertia = new Vector3f(0, 0, 0);
        physicalShape.calculateLocalInertia(mass, inertia);

        final MotionState motionState = new EntityMotionState(this);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo(mass, motionState, physicalShape,
                        inertia);
        physicalBox = new RigidBody(info);
    }

    private void randomize(final Vector3 vec) {
        vec.setX(Math.random());
        vec.setY(Math.random());
        vec.setZ(Math.random());
    }
}
