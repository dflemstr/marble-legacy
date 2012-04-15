package org.marble.entity.connected;

import java.util.Map;

import org.marble.entity.Entity;

/**
 * An entity that can be connected to other entities via one or more connectors.
 */
public interface Connected extends Entity {
    public Map<String, Connector> getConnectors();
}
