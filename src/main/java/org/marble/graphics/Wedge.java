package org.marble.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jme3.math.FastMath;
import com.jme3.scene.Mesh;
import com.jme3.util.BufferUtils;

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
        final float normalTopX, normalTopY, normalTopZ;
        final float normalBottomX, normalBottomY, normalBottomZ;
        if (angle > 0) {
            backY = FastMath.sin(angle) * heightExtent * 2;
            backZ = FastMath.cos(angle) * heightExtent;
        } else {
            backY = FastMath.sin(-angle) * heightExtent * 2;
            backZ = -FastMath.cos(angle) * heightExtent;
        }

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

        indices.put(7).put(9).put(8);

        // Top side
        positions.put(-widthExtent).put(0).put(heightExtent);
        positions.put(widthExtent).put(0).put(heightExtent);
        positions.put(-widthExtent).put(backY).put(backZ);
        positions.put(widthExtent).put(backY).put(backZ);

        // Bottom side
    }
}
