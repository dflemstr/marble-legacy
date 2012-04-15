package org.marble.entity.interactive;

import org.marble.entity.Entity;
import org.marble.input.PlayerInput;

/**
 * An interactive entity that reacts on user input.
 */
public interface Interactive extends Entity {
    public Iterable<PlayerInput> handledInputs();

    public void handleInput(PlayerInput input, boolean isActive);
}
