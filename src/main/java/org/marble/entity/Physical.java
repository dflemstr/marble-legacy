package org.marble.entity;

import jinngine.physics.Body;
import jinngine.physics.force.Force;

/**
 * An entity that has physical properties.
 */
public interface Physical extends Entity {
    /**
     * The body that represents this entity.
     */
    public Body getBody();

    /**
     * The forces that apply to this entity.
     */
    public Iterable<Force> getForces();
}
