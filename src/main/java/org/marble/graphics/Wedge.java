package org.marble.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jme3.math.FastMath;
import com.jme3.scene.Mesh;
import com.jme3.util.BufferUtils;
import com.jme3.util.TempVars;

public class Wedge extends Mesh {
    public Wedge(final float width, final float height, final float angle) {
        updateGeometry(width, height, angle);
    }

    private void updateGeometry(final float widthExtent,
            final float heightExtent, final float angle) {
        final FloatBuffer positions = BufferUtils.createVector3Buffer(14);
        final FloatBuffer normals = BufferUtils.createVector3Buffer(14);
        final FloatBuffer texcoords = BufferUtils.createVector2Buffer(14);
        final IntBuffer indices = BufferUtils.createIntBuffer(24);

        final float backY, backZ;
        if (angle > 0) {
            backY = FastMath.sin(angle) * heightExtent * 2;
            backZ = FastMath.cos(angle) * heightExtent;
        } else {
            backY = FastMath.sin(-angle) * heightExtent * 2;
            backZ = -FastMath.cos(angle) * heightExtent;
        }
        final TempVars vars = TempVars.get();
        vars.vect1.set(widthExtent, 0, 0);
        vars.vect2.set(0, heightExtent - backY, backZ);
        vars.vect1.crossLocal(vars.vect2);

        vars.vect3.set(-widthExtent, 0, 0);
        vars.vect4.set(0, backY - heightExtent, backZ);
        vars.vect3.crossLocal(vars.vect4);

        // Front
        positions.put(-widthExtent).put(0).put(-heightExtent);
        positions.put(widthExtent).put(0).put(-heightExtent);
        positions.put(-widthExtent).put(0).put(heightExtent);
        positions.put(widthExtent).put(0).put(heightExtent);

        normals.put(0).put(-1).put(0);
        normals.put(0).put(-1).put(0);
        normals.put(0).put(-1).put(0);
        normals.put(0).put(-1).put(0);

        texcoords.put(0).put(0);
        texcoords.put(1).put(0);
        texcoords.put(0).put(1);
        texcoords.put(1).put(1);

        indices.put(0).put(2).put(1);
        indices.put(2).put(1).put(3);

        // Left side
        positions.put(-widthExtent).put(0).put(-heightExtent);
        positions.put(-widthExtent).put(0).put(heightExtent);
        positions.put(-widthExtent).put(backY).put(backZ);

        normals.put(-1).put(0).put(0);
        normals.put(-1).put(0).put(0);
        normals.put(-1).put(0).put(0);

        texcoords.put(0).put(1);
        texcoords.put(0).put(0);
        texcoords.put(1).put(0.5f);

        indices.put(4).put(5).put(6);

        // Right side
        positions.put(widthExtent).put(0).put(-heightExtent);
        positions.put(widthExtent).put(0).put(heightExtent);
        positions.put(widthExtent).put(backY).put(backZ);

        normals.put(1).put(0).put(0);
        normals.put(1).put(0).put(0);
        normals.put(1).put(0).put(0);

        texcoords.put(0).put(0);
        texcoords.put(0).put(1);
        texcoords.put(1).put(0.5f);

        indices.put(7).put(9).put(8);

        // Top side
        positions.put(-widthExtent).put(0).put(heightExtent);
        positions.put(widthExtent).put(0).put(heightExtent);
        positions.put(-widthExtent).put(backY).put(backZ);
        positions.put(widthExtent).put(backY).put(backZ);

        normals.put(vars.vect1.x).put(vars.vect1.y).put(vars.vect1.z);
        normals.put(vars.vect1.x).put(vars.vect1.y).put(vars.vect1.z);
        normals.put(vars.vect1.x).put(vars.vect1.y).put(vars.vect1.z);
        normals.put(vars.vect1.x).put(vars.vect1.y).put(vars.vect1.z);

        texcoords.put(0).put(0);
        texcoords.put(1).put(0);
        texcoords.put(0).put(1);
        texcoords.put(1).put(1);

        indices.put(10).put(12).put(11);
        indices.put(11).put(12).put(13);

        // Bottom side
        positions.put(-widthExtent).put(0).put(-heightExtent);
        positions.put(widthExtent).put(0).put(-heightExtent);
        positions.put(-widthExtent).put(backY).put(backZ);
        positions.put(widthExtent).put(backY).put(backZ);

        normals.put(vars.vect3.x).put(vars.vect3.y).put(vars.vect3.z);
        normals.put(vars.vect3.x).put(vars.vect3.y).put(vars.vect3.z);
        normals.put(vars.vect3.x).put(vars.vect3.y).put(vars.vect3.z);
        normals.put(vars.vect3.x).put(vars.vect3.y).put(vars.vect3.z);

        texcoords.put(0).put(0);
        texcoords.put(1).put(0);
        texcoords.put(0).put(1);
        texcoords.put(1).put(1);

        indices.put(17).put(15).put(16);
        indices.put(15).put(16).put(14);
    }
}
