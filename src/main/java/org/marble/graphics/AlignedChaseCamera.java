package org.marble.graphics;

import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.math.FastMath;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

public class AlignedChaseCamera extends ChaseCamera {

    public AlignedChaseCamera(final Camera cam) {
        super(cam);
    }

    public AlignedChaseCamera(final Camera cam, final InputManager inputManager) {
        super(cam, inputManager);
    }

    public AlignedChaseCamera(final Camera cam, final Spatial target) {
        super(cam, target);
    }

    public AlignedChaseCamera(final Camera cam, final Spatial target,
            final InputManager inputManager) {
        super(cam, target, inputManager);
    }

    @Override
    protected void computePosition() {
        final float hDistance =
                (distance) * FastMath.sin((FastMath.PI / 2) - vRotation);
        pos.set(hDistance * FastMath.cos(rotation),
                -hDistance * FastMath.sin(rotation),
                (distance) * FastMath.sin(vRotation));
        pos.addLocal(target.getWorldTranslation());
    }
}
