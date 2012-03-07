package org.marble.graphics;

import com.ardor3d.input.control.OrbitCamControl;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.scenegraph.Spatial;

/**
 * An orbiting camera control that performs linear smoothing to make eased
 * transitions.
 */
public class SmoothOrbitCamControl extends OrbitCamControl {
    private final float trackSpeed;

    public SmoothOrbitCamControl(final Camera cam,
            final ReadOnlyVector3 target, final float trackSpeed) {
        super(cam, target);
        this.trackSpeed = trackSpeed;
    }

    public SmoothOrbitCamControl(final Camera cam, final Spatial target,
            final float trackSpeed) {
        super(cam, target);
        this.trackSpeed = trackSpeed;
    }

    @Override
    public void update(final double time) {
        updateTargetPos();

        if (!this._dirty)
            return;
        if (this._worldUpVec.getY() == 1) {
            MathUtils.sphericalToCartesian(this._sphereCoords,
                    this._camPosition);
        } else if (this._worldUpVec.getZ() == 1) {
            MathUtils.sphericalToCartesianZ(this._sphereCoords,
                    this._camPosition);
        }
        this._camPosition.addLocal(this._lookAtPoint);

        /**
         * Linearly smoothed camera tracking: {@code
         * |   x           |
         *                 ^ _camPosition (target)
         *     ^ new _camera location: (_camPos - _cam.loc) * trackSpeed * time + _cam.loc
         * ^ old _camera location
         * }
         */

        this._camPosition.subtractLocal(this._camera.getLocation());
        this._camPosition.multiplyLocal(this.trackSpeed * time);
        this._camPosition.addLocal(this._camera.getLocation());
        this._camera.setLocation(this._camPosition);

        this._camera.lookAt(this._lookAtPoint, this._worldUpVec);
    }
}
