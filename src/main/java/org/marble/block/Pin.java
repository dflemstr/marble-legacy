package org.marble.block;

import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
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

public class Pin extends AbstractEntity implements Graphical, Connected,
        Physical {

    private Node graphicalRails;
    private RigidBodyControl physicalBox;

    final float length;

    final float radius;

    public Pin(final float length) {
        this(length, 0.1f);
    }

    public Pin(final float length, final float radius) {
        this.length = length;
        this.radius = radius;
    }

    @Override
    public RigidBodyControl getBody() {
        return physicalBox;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return Connectors.fromRail(length, 0, radius * 2);
    }

    @Override
    public void initialize(final Game game) throws Exception {
        super.initialize(game);
        final AssetManager assetManager = game.getAssetManager();

        final Spatial left =
                new Geometry("pin", new Cylinder(10, 10, radius, length, true));
        left.setMaterial(assetManager
                .loadMaterial("Materials/Metal/Chrome.j3m"));

        final Matrix3f rotation = new Matrix3f(0, 0, -1, 0, 1, 0, 1, 0, 0);
        left.setLocalRotation(rotation);

        graphicalRails = new Node("rails");
        graphicalRails.attachChild(left);
        getSpatial().attachChild(graphicalRails);

        final CollisionShape leftCylinder =
                new BoxCollisionShape(new Vector3f(radius, radius, length / 2));

        final CompoundCollisionShape compound = new CompoundCollisionShape();
        compound.addChildShape(leftCylinder, new Vector3f(0, 0, 0), rotation);

        physicalBox = new RigidBodyControl(compound, 0);
        getSpatial().addControl(physicalBox);
    }

}
