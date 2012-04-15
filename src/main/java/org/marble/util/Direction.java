package org.marble.util;

import com.jme3.math.Vector3f;

/**
 * Represents a cardinal direction.
 */
public enum Direction {
    North(0, 1, 0), East(1, 0, 0), South(0, -1, 0), West(-1, 0, 0),
    Up(0, 0, 1), Down(0, 0, -1);
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
