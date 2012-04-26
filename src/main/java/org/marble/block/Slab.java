package org.marble.block;

import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.connected.Connected;
import org.marble.entity.connected.Connector;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Physical;
import org.marble.graphics.MaterialSP;
import org.marble.graphics.SegmentedBox;
import org.marble.util.Connectors;

/**
 * A box-shaped block.
 */
public class Slab extends AbstractEntity implements Connected, Graphical,
        Physical {
    private final float width, height, depth;
    private Spatial graphicalBox;
    private RigidBodyControl physicalBox;

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
        this(width, height, depth, Optional.<Float> absent());
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
    public Slab(final float width, final float height, final float depth,
            final Optional<Float> mass) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    @Override
    public void initialize(final Game game) {
        final AssetManager assetManager = game.getAssetManager();
        graphicalBox =
                new Geometry("slab", new SegmentedBox(1, 3, 0.3f,
                        Vector3f.ZERO, width / 2, height / 2, depth / 2));
        final Material material =
                new MaterialSP(
                        assetManager.loadMaterial("Materials/Organic/Wood.j3m"));

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
        graphicalBox.setMaterial(material);

        getSpatial().attachChild(graphicalBox);

        physicalBox =
                new RigidBodyControl(new BoxCollisionShape(new Vector3f(
                        width / 2, height / 2, depth / 2)), 0);
        getSpatial().addControl(physicalBox);
    }

    @Override
    public RigidBodyControl getBody() {
        return physicalBox;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return Connectors.fromBox(width, height, depth);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName())
                .add("width", width).add("height", height).add("depth", depth)
                .toString();
    }

    private void randomize(final Vector3f vec) {
        vec.setX((float) Math.random());
        vec.setY((float) Math.random());
        vec.setZ((float) Math.random());
    }
}
