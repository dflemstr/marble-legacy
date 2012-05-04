package org.marble.block;

import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.connected.Connected;
import org.marble.entity.connected.Connector;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Physical;
import org.marble.util.Connectors;

public class Rail extends AbstractEntity implements Connected, Graphical,
        Physical {

    final float width;
    final float height;
    final float depth;

    private RigidBodyControl physicalBox;

    private Node graphicalRails;

    public Rail(final float length) {
        this(length, 1f, 0.2f);
    }

    public Rail(final float width, final float height, final float depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    @Override
    public void initialize(final Game game) {
        final AssetManager assetManager = game.getAssetManager();

        final Material material =
                assetManager.loadMaterial("Materials/Metal/Chrome.j3m");
        final Spatial left =
                new Geometry("left rail",
                        new Cylinder(10, 10, depth / 2, width));
        left.setMaterial(material);

        final Spatial right =
                new Geometry("right rail", new Cylinder(10, 10, depth / 2,
                        width));
        right.setMaterial(material);

        final Matrix3f rotation = new Matrix3f(0, 0, -1, 0, 1, 0, 1, 0, 0);
        left.setLocalRotation(rotation);
        right.setLocalRotation(rotation);

        left.setLocalTranslation(0, height / 2, 0);
        right.setLocalTranslation(0, -height / 2, 0);

        graphicalRails = new Node("rails");
        graphicalRails.attachChild(left);
        graphicalRails.attachChild(right);
        getSpatial().attachChild(graphicalRails);

        final CollisionShape leftCylinder =
                new CylinderCollisionShape(new Vector3f(depth / 2, depth / 2,
                        width / 2));
        final CollisionShape rightCylinder =
                new CylinderCollisionShape(new Vector3f(depth / 2, depth / 2,
                        width / 2));

        final CompoundCollisionShape compound = new CompoundCollisionShape();
        compound.addChildShape(leftCylinder, new Vector3f(0, height / 2, 0),
                rotation);
        compound.addChildShape(rightCylinder, new Vector3f(0, -height / 2, 0),
                rotation);

        physicalBox = new RigidBodyControl(compound, 0);
        getSpatial().addControl(physicalBox);
    }

    @Override
    public RigidBodyControl getBody() {
        return physicalBox;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return Connectors.fromRail(width, height, depth);
    }
}
