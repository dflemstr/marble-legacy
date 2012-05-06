package org.marble.special;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import org.marble.Game;
import org.marble.ball.PlayerBall;
import org.marble.entity.AbstractEntity;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Collidable;
import org.marble.entity.physical.Physical;
import org.marble.graphics.GeoSphere;

public class PointOrb extends AbstractEntity implements Graphical, Physical,
        Collidable {
    private RigidBodyControl physicalBall;
    private Spatial graphicalBall;
    private final float radius;

    public PointOrb() {
        this(0.5f);
    }

    public PointOrb(final float radius) {
        this.radius = radius;
    }

    @Override
    public RigidBodyControl getBody() {
        return physicalBall;
    }

    @Override
    public void initialize(final Game game) {
        super.initialize(game);
        final AssetManager assetManager = game.getAssetManager();

        final GeoSphere geometricalBall =
                new GeoSphere(true, radius, 1, GeoSphere.TextureMode.Projected);

        graphicalBall = new Geometry("ball", geometricalBall);
        final Material transparent =
                assetManager.loadMaterial("Materials/Misc/Mesh.j3m");
        graphicalBall.setMaterial(transparent);
        getSpatial().attachChild(graphicalBall);

        final Spatial plus = assetManager.loadModel("Models/plus.obj");
        plus.setMaterial(assetManager.loadMaterial("Materials/Misc/Blue.j3m"));
        getSpatial().attachChild(plus);

        physicalBall =
                new RigidBodyControl(new SphereCollisionShape(radius), 1.0f);
        physicalBall.activate();
        physicalBall.setSleepingThresholds(0, 0);
        getSpatial().addControl(physicalBall);
    }

    @Override
    public void handleCollisionWith(final Physical other,
            final PhysicsCollisionEvent event) {
        if (other instanceof PlayerBall) {
            final int currentPoints =
                    game.getCurrentSession().get().getPoints();
            game.getCurrentSession().get().setPoints(currentPoints + 100);
            game.removeEntity(this);
        }
    }
}
