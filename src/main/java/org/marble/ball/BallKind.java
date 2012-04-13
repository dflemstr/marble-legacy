package org.marble.ball;

/**
 * A kind of ball material.
 */
public enum BallKind {
    /** A stone ball: heavy and slow */
    Stone(6.0, 0.5, 0.5, 4.0),

    /** A wooden ball: light and agile */
    Wood(1.0, 1.0, 1.0, 0.8),

    /** A fabric ball: very light and flimsy */
    Fabric(0.25, 2.0, 2.0, 0.5),

    /** An easily controlled ball that might break when moved too quickly */
    Glass(2.0, 1.0, 0.5, 1.0),

    /**
     * A ball that leaves a trail of mercury as it moves, slowly growing smaller
     */
    Mercury(8.0, 2.0, 16.0, 8.0);

    private static final double MASS_SCALE = 4.0;
    private static final double STABILITY_SCALE = 4.0;

    private final double mass;
    private final double linearDamping;
    private final double angularDamping;
    private final double stability;

    private BallKind(final double mass, final double linearDamping,
            final double angularDamping, final double stability) {
        this.mass = mass * MASS_SCALE;
        this.linearDamping = linearDamping;
        this.angularDamping = angularDamping;
        this.stability = stability * STABILITY_SCALE;
    }

    public double getAngularDamping() {
        return angularDamping;
    }

    public double getLinearDamping() {
        return linearDamping;
    }

    public double getMass() {
        return mass;
    }

    public double getStability() {
        return stability;
    }
}
