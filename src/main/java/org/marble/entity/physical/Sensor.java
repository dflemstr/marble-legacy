package org.marble.entity.physical;

import com.jme3.bullet.control.GhostControl;

public interface Sensor extends Actor {
    public Iterable<GhostControl> getSensors();
}
