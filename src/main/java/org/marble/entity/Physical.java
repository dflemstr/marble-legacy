package org.marble.entity;

import com.bulletphysics.dynamics.RigidBody;

/**
 * An entity that has physical properties.
 */
public interface Physical extends Entity {
    /**
     * The body that represents this entity.
     */
    public RigidBody getBody();
}
