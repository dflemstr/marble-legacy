package org.marble.entity;

import com.ardor3d.scenegraph.Spatial;

/**
 * A graphical entity that will be seen.
 */
public interface Graphical extends Entity {
    /**
     * The scene graph element that represents this element. The spatial's
     * transform will not be updated by the graphics engine; this must be
     * handled by the implementing class.
     */
    public Spatial getSpatial();
}
