package org.marble.entity;

import com.ardor3d.input.logical.InputTrigger;

/**
 * An interactive entity that reacts on user input.
 */
public interface Interactive extends Entity {
    /**
     * The triggers that trigger actions for this entity.
     */
    public Iterable<InputTrigger> getTriggers();
}
