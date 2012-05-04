package org.marble.block;

import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

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
    final Vector3f direction;
    final float a;
    final float b;

    private RigidBodyControl physicalBox;

    private Node graphicalRails;

    public Spiral(final float radius, final float height, final float angle) {
        this(radius, height, angle, 0, 0, 1, 90);
    }

    public Spiral(final float radius, final float height, final float angle,
            final float theta) {
        this(radius, height, angle, 0, 0, 1, theta);
    }

    public Spiral(final float radius, final float height, final float angle,
            final float x, final float y, final float z, final float theta) {
        this(radius, height, angle, 1, 0.1f, x, y, z, theta);
    }

    public Spiral(final float radius, final float height, final float angle,
            final float separation, final float tubeRadius, final float x,
            final float y, final float z, final float theta) {
        this.radius = radius;
        this.height = height;
        this.angle = angle * FastMath.DEG_TO_RAD;
        this.theta = theta;
        this.separation = separation;
        this.tubeRadius = tubeRadius;
        direction = new Vector3f(x, y, z).normalize();
        a = FastMath.sin(FastMath.DEG_TO_RAD * theta) * separation;
        b = FastMath.cos(FastMath.DEG_TO_RAD * theta) * separation;
    }

    @Override
    public void initialize(final Game game) {
        final float pi = (float) Math.PI;
        final int steps = (int) (angle / (pi / 6) * (radius));
        final AssetManager assetManager = game.getAssetManager();

        final Spatial left =
                new Geometry("left rail", new Curve(steps, 10, radius - a / 2,
                        height, angle, tubeRadius, direction));
        left.setMaterial(assetManager
                .loadMaterial("Materials/Metal/Aluminium.j3m"));

        final Spatial right =
                new Geometry("right rail", new Curve(steps, 10, radius + a / 2,
                        height, angle, tubeRadius, direction));
        right.setMaterial(assetManager
                .loadMaterial("Materials/Metal/Aluminium.j3m"));
        left.setLocalTranslation(direction.mult(-b / 2));
        right.setLocalTranslation(direction.mult(b / 2));
        graphicalRails = new Node("rails");
        graphicalRails.attachChild(left);
        graphicalRails.attachChild(right);
        getSpatial().attachChild(graphicalRails);

        final CompoundCollisionShape compound = new CompoundCollisionShape();

        Vector3f n;
        if (direction.equals(Vector3f.UNIT_X)) {
            n = direction.cross(Vector3f.UNIT_Y);
            n.normalizeLocal();
        } else {
            n = direction.cross(Vector3f.UNIT_X);
            n.normalizeLocal();
        }

        final Matrix3f rotTot = new Matrix3f();
        final Matrix3f rotTot2 = new Matrix3f();
        rotTot2.fromAngleAxis(-direction.angleBetween(Vector3f.UNIT_Z),
                direction.cross(Vector3f.UNIT_Z).normalize());

        final Matrix3f temp = new Matrix3f();
        // temp.fromAngleAxis(direction.angleBetween(Vector3f.UNIT_Z), n);
        // temp.mult(rotTot2, rotTot2);
        temp.fromAngleAxis(pi / 2 + FastMath.atan(height / (radius * angle)), n);
        temp.mult(rotTot2, rotTot2);
        final Matrix3f rotZ = new Matrix3f();
        rotZ.fromAngleAxis(angle / steps / 2, direction);
        rotZ.mult(rotTot, rotTot);
        rotZ.mult(rotTot2, rotTot2);
        rotZ.fromAngleAxis(angle / steps, direction);
        Vector3f radialAxis = new Vector3f();
        final Vector3f leftMiddle = new Vector3f();
        final Vector3f rightMiddle = new Vector3f();

        for (int i = 0; i < steps; i++) {

            final float fraction = ((float) i) / steps;
            radialAxis = rotTot.mult(n);
            radialAxis.mult(radius - a / 2, leftMiddle);
            radialAxis.mult(radius + a / 2, rightMiddle);
            leftMiddle.addLocal(direction.mult(height * fraction));
            rightMiddle.addLocal(direction.mult(height * fraction));

            final double theta = angle / steps;

            final CylinderCollisionShape leftCylinder =
                    new CylinderCollisionShape(
                            new Vector3f(
                                    tubeRadius,
                                    tubeRadius,
                                    (float) Math.sqrt((Math.sin(theta) * (radius - a / 2))
                                            * (Math.sin(theta) * (radius - a / 2))
                                            + direction.mult(height / steps)
                                                    .getZ()
                                            * direction.mult(height / steps)
                                                    .getZ()) / 2));
            final CylinderCollisionShape rightCylinder =
                    new CylinderCollisionShape(
                            new Vector3f(
                                    tubeRadius,
                                    tubeRadius,
                                    (float) Math.sqrt((Math.sin(theta) * (radius + a / 2))
                                            * (Math.sin(theta) * (radius + a / 2))
                                            + direction.mult(height / steps)
                                                    .getZ()
                                            * direction.mult(height / steps)
                                                    .getZ()) / 2));

            compound.addChildShape(leftCylinder,
                    leftMiddle.add(direction.mult(-b / 2)), rotTot2);
            compound.addChildShape(rightCylinder,
                    rightMiddle.add(direction.mult(b / 2)), rotTot2);

            rotZ.mult(rotTot, rotTot);
            rotZ.mult(rotTot2, rotTot2);
        }

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
        return Connectors.fromSpiral(radius, height, tubeRadius, separation,
                angle, direction, a, b);
    }

}
