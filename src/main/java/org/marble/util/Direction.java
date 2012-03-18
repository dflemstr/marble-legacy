package org.marble.util;

import javax.vecmath.Vector3f;

/**
 * Represents a cardinal direction.
 */
public enum Direction {
    NORTH(0, 1, 0), EAST(1, 0, 0), SOUTH(0, -1, 0), WEST(-1, 0, 0),
    UP(0, 0, 1), DOWN(0, 0, -1);
    private Vector3f physicalDirection;

    private Direction(final float x, final float y, final float z) {
        physicalDirection = new Vector3f(x, y, z);
    }

    /**
     * A normalized vector representing the physical direction of this cardinal
     * direction.
     *
     * @return A mutable reference to the physical direction. This vector must
     *         not be altered.
     */
    public Vector3f getPhysicalDirection() {
        return physicalDirection;
    }
}
