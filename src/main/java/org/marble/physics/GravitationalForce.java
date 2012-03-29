package org.marble.physics;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.force.Force;

public class GravitationalForce implements Force {
    private final Body body;
    private final Vector3 force;
    private final Vector3 zero = new Vector3();
    private static final Vector3 standardGravity = new Vector3(0, 0, -1);

    public GravitationalForce(final Body body) {
        this(body, standardGravity);
    }

    public GravitationalForce(final Body body, final Vector3 force) {
        this.body = body;
        this.force = force;
    }

    @Override
    public void apply(final double dt) {
        body.applyForce(zero, body.state.anisotropicmass.multiply(force), dt);
    }

}
