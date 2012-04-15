package org.marble.block;

import java.util.Map;

import javax.vecmath.Vector3f;

import com.ardor3d.math.Matrix3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Cylinder;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CylinderShapeX;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;

import org.marble.entity.AbstractEntity;
import org.marble.entity.Connected;
import org.marble.entity.Connector;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;
import org.marble.util.Connectors;

public class Rail extends AbstractEntity implements Connected, Graphical,
        Physical {

    final double width;
    final double height;
    final double depth;
    final double angle;

    private final RigidBody physicalBox;

    private final Cylinder left;
    private final Cylinder right;

    public Rail(final double length) {
        this(length, 0.7, 0.3, 0);
    }

    public Rail(final double width, final double height, final double depth,
            final double angle) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.angle = angle;

        left = new Cylinder("left rail", 10, 10, depth / 2, width);
        right = new Cylinder("right rail", 10, 10, depth / 2, width);

        left.setRotation(new Matrix3(0, 0, -1, 0, 1, 0, 1, 0, 0));
        right.setRotation(new Matrix3(0, 0, -1, 0, 1, 0, 1, 0, 0));

        left.setTranslation(0, height / 2, 0);
        right.setTranslation(0, -height / 2, 0);

        final CompoundShape compound = new CompoundShape();

        final CollisionShape leftCylinder =
                new CylinderShapeX(new Vector3f((float) width / 2,
                        (float) depth / 2, (float) depth / 2));
        final CollisionShape rightCylinder =
                new CylinderShapeX(new Vector3f((float) width / 2,
                        (float) depth / 2, (float) depth / 2));

        final Transform r = new Transform();
        final Transform l = new Transform();

        r.setIdentity();
        l.setIdentity();

        l.origin.set(0, (float) height / 2, 0);
        r.origin.set(0, (float) -height / 2, 0);

        compound.addChildShape(l, leftCylinder);
        compound.addChildShape(r, rightCylinder);

        final Vector3f inertia = new Vector3f(0, 0, 0);
        compound.calculateLocalInertia(0.0f, inertia);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo(0.0f, null, compound);

        physicalBox = new RigidBody(info);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.marble.entity.Graphical#getSpatial()
     */
    @Override
    public Spatial getSpatial() {
        final Node node = new Node();
        node.attachChild(left);
        node.attachChild(right);
        return node;
    }

    @Override
    public RigidBody getBody() {
        return physicalBox;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.marble.entity.Connected#getConnectors()
     */
    @Override
    public Map<String, Connector> getConnectors() {
        return Connectors.fromBox(width, height, depth);
    }

}
