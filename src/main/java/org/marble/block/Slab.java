package org.marble.block;

import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.util.TempVars;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.connected.Connected;
import org.marble.entity.connected.Connector;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Physical;
import org.marble.graphics.SegmentedBox;
import org.marble.util.Connectors;
import org.marble.util.OfflineTransforms;

/**
 * A box-shaped block.
 */
public class Slab extends AbstractEntity implements Connected, Graphical,
        Physical {
    private final float width, height, depth;
    private final float slopeX, slopeY;
    private Geometry graphicalBox;
    private RigidBodyControl physicalBox;

    public Slab(final float width, final float height, final float depth) {
        this(width, height, depth, 0, 0);
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
     */
    public Slab(final float width, final float height, final float depth,
            final float slopeX, final float slopeY) {
        this(width, height, depth, slopeX, slopeY, Optional.<Float> absent());
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
            final float slopeX, final float slopeY, final float mass) {
        this(width, height, depth, slopeX, slopeY, Optional.of(mass));
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
            final float slopeX, final float slopeY, final Optional<Float> mass) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.slopeX = slopeX;
        this.slopeY = slopeY;
    }

    @Override
    public void initialize(final Game game) {
        final AssetManager assetManager = game.getAssetManager();
        final Mesh graphicalMesh =
                new SegmentedBox(1, 2, 0.25f, Vector3f.ZERO, width / 2,
                        height / 2, depth / 2);
        final Mesh physicalMesh =
                new Box(Vector3f.ZERO, width / 2, height / 2, depth / 2);
        boolean changed = false;

        final TempVars vars = TempVars.get();
        vars.tempMat4.loadIdentity();
        vars.tempMat4.set(2, 0, slopeX);
        OfflineTransforms.transformNonLinear3D(
                graphicalMesh.getFloatBuffer(Type.Position), vars.tempMat4);
        OfflineTransforms.transformNonLinear3D(
                physicalMesh.getFloatBuffer(Type.Position), vars.tempMat4);

        vars.tempMat4.loadIdentity();
        vars.tempMat4.set(0, 3, -slopeX);
        OfflineTransforms.transformNonLinear3DNorm(
                graphicalMesh.getFloatBuffer(Type.Normal), vars.tempMat4);
        OfflineTransforms.transformNonLinear3DNorm(
                physicalMesh.getFloatBuffer(Type.Normal), vars.tempMat4);
        changed = true;

        vars.tempMat4.loadIdentity();
        vars.tempMat4.set(2, 1, slopeY);
        OfflineTransforms.transformNonLinear3D(
                graphicalMesh.getFloatBuffer(Type.Position), vars.tempMat4);
        OfflineTransforms.transformNonLinear3D(
                physicalMesh.getFloatBuffer(Type.Position), vars.tempMat4);

        vars.tempMat4.loadIdentity();
        vars.tempMat4.set(1, 3, -slopeY);
        OfflineTransforms.transformNonLinear3DNorm(
                graphicalMesh.getFloatBuffer(Type.Normal), vars.tempMat4);
        OfflineTransforms.transformNonLinear3DNorm(
                physicalMesh.getFloatBuffer(Type.Normal), vars.tempMat4);
        changed = true;
        vars.release();

        if (changed) {
            graphicalMesh.updateBound();
            physicalMesh.updateBound();
        }

        graphicalBox = new Geometry("slab", graphicalMesh);
        final Material material =
                assetManager.loadMaterial("Materials/Mineral/Concrete.j3m");
        graphicalBox.setMaterial(material);

        getSpatial().attachChild(graphicalBox);

        final CollisionShape shape;
        if (changed) {
            shape = new MeshCollisionShape(physicalMesh);
        } else {
            shape =
                    new BoxCollisionShape(new Vector3f(width / 2, height / 2,
                            depth / 2));
        }
        physicalBox = new RigidBodyControl(shape, 0);
        getSpatial().addControl(physicalBox);
    }

    @Override
    public RigidBodyControl getBody() {
        return physicalBox;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return Connectors.fromBox(width, height, depth, slopeX, slopeY);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", getName())
                .add("width", width).add("height", height).add("depth", depth)
                .toString();
    }
}
