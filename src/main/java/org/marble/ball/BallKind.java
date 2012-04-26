package org.marble.ball;

/**
 * A kind of ball material.
 */
public enum BallKind {
    /** A stone ball: heavy and slow */
    Stone(6.0f, 0.5f, 0.5f, 4.0f, 0.5f, 0),

    /** A wooden ball: light and agile */
    Wood(1.0f, 1.0f, 1.0f, 0.8f, 0.5f, 0),

    /** A fabric ball: very light and flimsy */
    Fabric(0.25f, 2.0f, 2.0f, 0.2f, 0.5f, 0),

    /** An easily controlled ball that might break when moved too quickly */
    Glass(2.0f, 1.0f, 0.5f, 1.0f, 0.5f, 0),

    /**
     * A ball that leaves a trail of mercury as it moves, slowly growing smaller
     */
    Mercury(8.0f, 2.0f, 16.0f, 8.0f, 0.5f, 0);

    private static final float MASS_SCALE = 4.0f;
    private static final float STABILITY_SCALE = 4.0f;

    private final float mass;
    private final float linearDamping;
    private final float angularDamping;
    private final float stability;
    private final float friction;
    private final float restitution;

    private BallKind(final float mass, final float linearDamping,
            final float angularDamping, final float stability,
            final float friction, final float restitution) {
        this.mass = mass * MASS_SCALE;
        this.linearDamping = linearDamping;
        this.angularDamping = angularDamping;
        this.stability = stability * STABILITY_SCALE;
        this.friction = friction;
        this.restitution = restitution;
    }

    public float getAngularDamping() {
        return angularDamping;
    }

    public float getLinearDamping() {
        return linearDamping;
    }

    public float getMass() {
        return mass;
    }

    public float getStability() {
        return stability;
    }

    public float getFriction() {
        return friction;
    }

    public float getRestitution() {
        return restitution;
    }
}
