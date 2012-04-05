package org.marble.entity;

import com.ardor3d.light.Light;

public interface Emitter extends Graphical {
    public Light getLight();
}
