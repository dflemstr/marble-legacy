package org.marble.util;

import java.nio.FloatBuffer;

import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import com.jme3.util.TempVars;

public final class OfflineTransforms {
    private OfflineTransforms() {
    }

    public static void transformNonLinear3D(final FloatBuffer buffer,
            final Matrix4f matrix) {
        final TempVars vars = TempVars.get();
        final Vector3f vector = vars.vect1;

        for (int i = 0; i < buffer.limit() / 3; i++) {
            BufferUtils.populateFromBuffer(vector, buffer, i);
            matrix.mult(vector, vector);
            BufferUtils.setInBuffer(vector, buffer, i);
        }

        vars.release();
    }

    public static void transformNonLinear3DNorm(final FloatBuffer buffer,
            final Matrix4f matrix) {
        final TempVars vars = TempVars.get();
        final Vector3f vector = vars.vect1;

        for (int i = 0; i < buffer.limit() / 3; i++) {
            BufferUtils.populateFromBuffer(vector, buffer, i);
            matrix.mult(vector, vector);
            vector.normalizeLocal();
            BufferUtils.setInBuffer(vector, buffer, i);
        }

        vars.release();

    }
}
