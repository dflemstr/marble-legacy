package org.marble.ball;

/**
 * A kind of ball material.
 */
public enum BallKind {
    /** A stone ball: heavy and slow */
    Stone(2, 0.5f),

    /** A wooden ball: light and agile */
    Wood(1, 1),

    /** A fabric ball: very light and flimsy */
    Fabric(0.5f, 2.0f),

    /** An easily controlled ball that might break when moved too quickly */
    Glass(0.75f, 1),

    /**
     * A ball that leaves a trail of mercury as it moves, slowly growing smaller
     */
    Mercury(4, 0.25f);

    private float mass;

    private float maxAngle;

    private double acceleration;

    private BallKind(final float mass, final float maxAngle) {
        this.mass = mass;
        this.maxAngle = maxAngle;
        acceleration = maxAngle * 10;
    }

    public float getMass() {
        return mass;
    }

    public double maxAcceleration() {
        return acceleration;

    }

}
