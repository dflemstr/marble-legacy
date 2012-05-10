package org.marble.entity.physical;

import com.jme3.bullet.collision.PhysicsCollisionEvent;

/**
 * A collidable entity that can collide with other physical entities.
 */
public interface Collidable extends Physical {
    /**
     * Called when this entity is in contact with another entity.
     * 
     * @param other
     *            The other entity that this entity is in contact with.
     */
    public void
            handleCollisionWith(Physical other, PhysicsCollisionEvent event);
}
