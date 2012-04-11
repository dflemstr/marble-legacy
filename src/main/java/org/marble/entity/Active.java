package org.marble.entity;

import org.marble.physics.Force;

public interface Active {
    public Iterable<Force> getForces();
}
