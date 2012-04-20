package org.marble.entity.graphical;

import com.jme3.light.Light;


public interface Emitter extends Graphical {
    public Iterable<Light> getLights();
}
