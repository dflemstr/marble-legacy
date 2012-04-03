package org.marble.entity;

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
    public void handleContactAdded(Physical other);

    /**
     * Called when this entity is no longer in contact with the other entity.
     * 
     * @param other
     *            The entity that this entity no longer is in contact with.
     */
    public void handleContactRemoved(Physical other);
}
