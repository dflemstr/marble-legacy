package org.marble.block;

import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import com.google.common.collect.ImmutableMap;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.connected.Connected;
import org.marble.entity.connected.Connector;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Physical;
import org.marble.graphics.Curve;
import org.marble.util.Connectors;

public class Spiral extends AbstractEntity implements Connected, Graphical,
        Physical {

    final float radius;
    final float height;
    final float angle;
    final float theta;
    final float separation;
    final float tubeRadius;

    private RigidBodyControl physicalBox;

    private Node graphicalRails;

    public Spiral(final float radius, final float height, final float angle) {
        this(radius, height, angle, 90);
    }

    public Spiral(final float radius, final float height, final float angle,
            final float theta) {
        this(radius, height, angle, 1, 0.1f, theta);
    }

    public Spiral(final float radius, final float height, final float angle,
            final float separation, final float tubeRadius, final float theta) {
        this.radius = radius;
        this.height = height;
        this.angle = angle * FastMath.DEG_TO_RAD;
        this.theta = theta * FastMath.DEG_TO_RAD;
        this.separation = separation;
        this.tubeRadius = tubeRadius;
    }

    @Override
    public void initialize(final Game game) {
        final float pi = (float) Math.PI;
        final int steps = (int) (angle / (pi / 6) * (radius));
        final AssetManager assetManager = game.getAssetManager();

        final float railOffsetY = FastMath.sin(theta) * separation;
        final float railOffsetX = FastMath.cos(theta) * separation;
        final Mesh curveLeft =
                new Curve(steps, 10, radius - railOffsetX / 2, height, angle,
                        tubeRadius);
        final Mesh curveRight =
                new Curve(steps, 10, radius + railOffsetX / 2, height, angle,
                        tubeRadius);

        final Spatial left = new Geometry("left rail", curveLeft);
        left.setMaterial(assetManager
                .loadMaterial("Materials/Misc/Undefined.j3m"));

        final Spatial right = new Geometry("right rail", curveRight);
        right.setMaterial(assetManager
                .loadMaterial("Materials/Misc/Undefined.j3m"));
        left.setLocalTranslation(-railOffsetX / 2, -railOffsetY / 2, 0);
        right.setLocalTranslation(railOffsetX / 2, railOffsetY / 2, 0);
        graphicalRails = new Node("rails");
        graphicalRails.attachChild(left);
        graphicalRails.attachChild(right);
        getSpatial().attachChild(graphicalRails);

        final CompoundCollisionShape compound = new CompoundCollisionShape();

        compound.addChildShape(new MeshCollisionShape(curveLeft), new Vector3f(
                -railOffsetX / 2, -railOffsetY / 2, 0));
        compound.addChildShape(new MeshCollisionShape(curveRight),
                new Vector3f(railOffsetX / 2, railOffsetY / 2, 0));

        physicalBox = new RigidBodyControl(compound, 0);
        getSpatial().addControl(physicalBox);

    }

    @Override
    public RigidBodyControl getBody() {
        return physicalBox;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.marble.entity.Connected#getConnectors()
     */
    @Override
    public Map<String, Connector> getConnectors() {
        return ImmutableMap.of("start-middle", Connectors.offsetBy(0, 0, 0));
    }
}
