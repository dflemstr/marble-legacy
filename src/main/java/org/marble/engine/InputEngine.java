package org.marble.engine;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.util.ReadOnlyTimer;

import org.marble.entity.Interactive;

/**
 * The Ardor3D-based input engine.
 */
public class InputEngine extends Engine<Interactive> {
    /**
     * Marks the engine for shutdown on activation.
     */
    private class QuitAction implements TriggerAction {

        @Override
        public void perform(final Canvas source,
                final TwoInputStates inputStates, final double tpf) {
            shouldContinue = false;
        }

    }

    private final LogicalLayer logicalLayer;
    private final PhysicalLayer physicalLayer;
    private final MouseManager mouseManager;

    private boolean shouldContinue = true;

    /**
     * Creates a new input engine.
     * 
     * @param logicalLayer
     *            The layer for capturing input from.
     * @param mouseManager
     *            The manager for controlling the mouse.
     */
    public InputEngine(final LogicalLayer logicalLayer,
            final PhysicalLayer physicalLayer,
            final MouseManager mouseManager) {
        super(Interactive.class);
        this.logicalLayer = logicalLayer;
        this.physicalLayer = physicalLayer;
        this.mouseManager = mouseManager;
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    protected void entityAdded(final Interactive entity) {
        for (final InputTrigger trigger : entity.getTriggers()) {
            logicalLayer.registerTrigger(trigger);
        }
    }

    @Override
    protected void entityRemoved(final Interactive entity) {
        for (final InputTrigger trigger : entity.getTriggers()) {
            logicalLayer.deregisterTrigger(trigger);
        }
    }

    public LogicalLayer getLogicalLayer() {
        return logicalLayer;
    }

    public MouseManager getMouseManager() {
        return mouseManager;
    }

    @Override
    public void initialize() {

    }

    @Override
    public boolean update(final ReadOnlyTimer timer) {
        logicalLayer.checkTriggers(timer.getTimePerFrame());
        return shouldContinue;
    }

    public PhysicalLayer getPhysicalLayer() {
        return physicalLayer;
    }

}
