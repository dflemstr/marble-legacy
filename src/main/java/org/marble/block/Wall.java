package org.marble.block;

import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.connected.Connected;
import org.marble.entity.connected.Connector;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Physical;
import org.marble.util.Connectors;

public class Wall extends AbstractEntity implements Connected, Graphical,
        Physical {

    private final float width, height, depth;
    private RigidBodyControl physicalBox;
    private Geometry graphicalBox;

    public Wall(final float width) {
        this(width, 0.5f, 1.0f);
    }

    public Wall(final float width, final float height, final float depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    @Override
    public void initialize(final Game game) {
        final AssetManager assetManager = game.getAssetManager();

        graphicalBox =
                new Geometry("wall", new Box(Vector3f.ZERO, width / 2,
                        height / 2, depth / 2));
        graphicalBox.setMaterial(assetManager
                .loadMaterial("Materials/Misc/Undefined.j3m"));
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
}
