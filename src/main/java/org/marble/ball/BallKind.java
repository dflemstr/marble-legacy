package org.marble.ball;

/**
 * A kind of ball material.
 */
public enum BallKind {
    /** A stone ball: heavy and slow */
    Stone,

    /** A wooden ball: light and agile */
    Wood,

    /** A fabric ball: very light and flimsy */
    Fabric,

    /** An easily controlled ball that might break when moved too quickly */
    Glass,

    /**
     * A ball that leaves a trail of mercury as it moves, slowly growing smaller
     */
    Mercury;
}
