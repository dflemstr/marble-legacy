package org.marble.entity;

import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.RigidBody;

/**
 * An entity that has physical properties.
 */
public interface Physical extends Entity {
    /**
     * Actions that should be performed on each physics simulation iteration.
     */
    public Iterable<ActionInterface> getActions();

    /**
     * The body that represents this entity.
     */
    public RigidBody getBody();
}
