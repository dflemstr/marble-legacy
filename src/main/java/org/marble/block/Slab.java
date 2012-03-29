package org.marble.block;

import java.util.Map;

import jinngine.physics.Body;
import jinngine.physics.force.Force;

import com.ardor3d.image.Texture;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import org.marble.entity.AbstractEntity;
import org.marble.entity.Connectivity;
import org.marble.entity.Connector;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;
import org.marble.graphics.EntityController;
import org.marble.graphics.SegmentedBox;
import org.marble.util.Connectors;
import org.marble.util.Shaders;

/**
 * A box-shaped block.
 */
public class Slab extends AbstractEntity implements Connectivity, Graphical,
        Physical {
    private final double width, height, depth;
    private final Box graphicalBox;
    private final Body physicalBox;
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
    public Slab(final double width, final double height, final double depth) {
        this(width, height, depth, Optional.<Double> absent());
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
    public Slab(final double width, final double height, final double depth,
            final double mass) {
        this(width, height, depth, Optional.of(mass));
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
    public Slab(final double width, final double height, final double depth,
            final Optional<Double> mass) {
        this.width = width;
        this.height = height;
        this.depth = depth;

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

        graphicalBox =
                new SegmentedBox("slab", 1, 1, 0.3, Vector3.ZERO, width / 2,
                        height / 2, depth / 2);
        graphicalBox.setRenderState(wood);
        graphicalBox.setRenderState(ts);
        graphicalBox.addController(new EntityController(this));

        final jinngine.geometry.Box geometricalBox =
                new jinngine.geometry.Box(width, height, depth);

        physicalBox = new Body("slab", geometricalBox);

        if (mass.isPresent()) {
            geometricalBox.setMass(mass.get());
        } else {
            physicalBox.setFixed(true);
        }
    }

    @Override
    public Body getBody() {
        return physicalBox;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return Connectors.fromBox(width, height, depth);
    }

    @Override
    public Iterable<Force> getForces() {
        return ImmutableSet.of();
    }

    @Override
    public Spatial getSpatial() {
        return graphicalBox;
    }

    private void randomize(final Vector3 vec) {
        vec.setX(Math.random());
        vec.setY(Math.random());
        vec.setZ(Math.random());
    }
}
