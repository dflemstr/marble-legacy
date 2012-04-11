package org.marble.physics;

import javax.vecmath.Vector3f;

public interface Force {
    public void calculateForce(Vector3f out);
}
