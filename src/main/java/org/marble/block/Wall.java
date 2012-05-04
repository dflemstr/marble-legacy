package org.marble.block;

import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Cylinder;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.connected.Connected;
import org.marble.entity.connected.Connector;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Physical;
import org.marble.util.Connectors;

public class Wall extends AbstractEntity implements Connected, Graphical,
        Physical {

    private final float length;
    private RigidBodyControl physicalBox;
    private Geometry graphicalBox;

    public Wall(final float length) {
        this.length = length;
    }

    @Override
    public void initialize(final Game game) {
        final AssetManager assetManager = game.getAssetManager();

        graphicalBox = new Geometry("wall", new Cylinder(3, 8, 0.05f, length));
        graphicalBox.setMaterial(assetManager
                .loadMaterial("Materials/Metal/Chrome.j3m"));
        final Matrix3f rotation = new Matrix3f(0, 0, -1, 0, 1, 0, 1, 0, 0);
        graphicalBox.setLocalRotation(rotation);
        getSpatial().attachChild(graphicalBox);
        final CollisionShape wall =
                new CylinderCollisionShape(new Vector3f(0.05f, 0.05f,
                        length / 2));

        final CompoundCollisionShape compound = new CompoundCollisionShape();
        compound.addChildShape(wall, new Vector3f(0, 0, 0), rotation);

        physicalBox = new RigidBodyControl(compound, 0);
        getSpatial().addControl(physicalBox);
    }

    @Override
    public RigidBodyControl getBody() {
        return physicalBox;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return Connectors.fromWall(length);
    }
}
