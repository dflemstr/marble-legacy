package org.marble.graphics;

import com.ardor3d.input.control.OrbitCamControl;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.scenegraph.Spatial;

import org.marble.util.DoubleRingBuffer;

/**
 * An orbiting camera control that performs linear smoothing to make eased
 * transitions.
 */
public class SmoothOrbitCamControl extends OrbitCamControl {
    private final double trackSpeed;
    private final DoubleRingBuffer timeBuffer = new DoubleRingBuffer(64);

    public SmoothOrbitCamControl(final Camera cam,
            final ReadOnlyVector3 target, final double trackSpeed) {
        super(cam, target);
        this.trackSpeed = trackSpeed;
    }

    public SmoothOrbitCamControl(final Camera cam, final Spatial target,
            final double trackSpeed) {
        super(cam, target);
        this.trackSpeed = trackSpeed;
    }

    @Override
    public void update(final double varyingTime) {
        updateTargetPos();
        timeBuffer.insert(varyingTime);
        final double time = timeBuffer.getAverage();

        if (!_dirty)
            return;
        if (_worldUpVec.getY() == 1) {
            MathUtils.sphericalToCartesian(_sphereCoords, _camPosition);
        } else if (_worldUpVec.getZ() == 1) {
            MathUtils.sphericalToCartesianZ(_sphereCoords, _camPosition);
        }
        _camPosition.addLocal(_lookAtPoint);

        /**
         * Linearly smoothed camera tracking: {@code
         * |   x           |
         *                 ^ _camPosition (target)
         *     ^ new _camera location: (_camPos - _cam.loc) * trackSpeed * time + _cam.loc
         * ^ old _camera location
         * }
         */

        _camPosition.subtractLocal(_camera.getLocation());
        _camPosition.multiplyLocal(trackSpeed * time);
        _camPosition.addLocal(_camera.getLocation());
        _camera.setLocation(_camPosition);

        _camera.lookAt(_lookAtPoint, _worldUpVec);
    }
}
