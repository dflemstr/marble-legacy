package org.marble.entity;

import java.util.Map;

/**
 * An entity that can be connected to other entities via one or more connectors.
 */
public interface Connected extends Entity {
    public Map<String, Connector> getConnectors();
}
