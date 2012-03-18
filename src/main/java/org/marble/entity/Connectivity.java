package org.marble.entity;

import java.util.Map;

public interface Connectivity extends Entity {
    public Map<String, Connector> getConnectors();
}
