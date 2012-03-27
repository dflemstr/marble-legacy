package org.marble.settings;

import java.util.prefs.Preferences;

import org.marble.RendererImpl;

/**
 * A renderer settings entry.
 */
class RendererEntry extends Entry<RendererImpl> {

    RendererEntry(final Preferences prefs, final String node,
            final RendererImpl defaultValue) {
        super(prefs, node, defaultValue);
    }

    @Override
    public RendererImpl getValue() {
        return RendererImpl.valueOf(prefs.get(node, defaultValue.name()));
    }

    @Override
    public void setValue(final RendererImpl value) {
        prefs.put(node, value.name());
    }

}