package org.marble.entity.physical;

import com.jme3.bullet.control.RigidBodyControl;

import org.marble.entity.Entity;

/**
 * An entity that has physical properties.
 */
public interface Physical extends Entity {
    /**
     * The body that represents this entity.
     */
    public RigidBodyControl getBody();
}
