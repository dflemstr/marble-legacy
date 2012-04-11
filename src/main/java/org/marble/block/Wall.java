package org.marble.block;

import java.util.Map;

import javax.vecmath.Vector3f;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Box;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;

import org.marble.entity.AbstractEntity;
import org.marble.entity.Connected;
import org.marble.entity.Connector;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;
import org.marble.util.Connectors;

public class Wall extends AbstractEntity implements Connected, Graphical,
        Physical {

    private final double width, height, depth;
    private final Box graphicalBox;
    private final RigidBody physicalBox;

    public Wall(final double width, final double height, final double depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        graphicalBox =
                new Box("wall", new Vector3(0, 0, depth / 2), width / 2,
                        height / 2, depth / 2);
        final CollisionShape geometricalBox =
                new BoxShape(new Vector3f((float) width / 2,
                        (float) height / 2, (float) depth));

        final Vector3f inertia = new Vector3f(0, 0, 0);
        geometricalBox.calculateLocalInertia(0.0f, inertia);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo(0.0f, new DefaultMotionState(),
                        geometricalBox, inertia);
        physicalBox = new RigidBody(info);
    }

    @Override
    public Spatial getSpatial() {
        return graphicalBox;
    }

    @Override
    public RigidBody getBody() {
        return physicalBox;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return Connectors.fromBox(width, height, depth);
    }
}
