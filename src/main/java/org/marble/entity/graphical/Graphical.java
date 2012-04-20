package org.marble.entity.graphical;

import com.jme3.scene.Spatial;

import org.marble.entity.Entity;

/**
 * A graphical entity that will be seen.
 */
public interface Graphical extends Entity {
    Spatial getSpatial();
}
