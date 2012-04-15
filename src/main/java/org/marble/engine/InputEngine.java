package org.marble.engine;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.TouchInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.InputListener;
import com.jme3.system.JmeContext;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.marble.entity.interactive.Interactive;
import org.marble.input.PlayerInput;

/**
 * The Ardor3D-based input engine.
 */
public class InputEngine extends Engine<Interactive> {

    private final MouseInput mouseInput;
    private final KeyInput keyInput;
    private final TouchInput touchInput;

    private final InputManager inputManager;

    /**
     * @return the inputManager
     */
    public InputManager getInputManager() {
        return inputManager;
    }

    private final Multimap<Interactive, ActionListener> listeners =
            HashMultimap.create();

    /**
     * Creates a new input engine.
     */
    public InputEngine(final JmeContext context) {
        super(Interactive.class);
        mouseInput = context.getMouseInput();
        keyInput = context.getKeyInput();
        touchInput = context.getTouchInput();

        inputManager = new InputManager(mouseInput, keyInput, null, touchInput);
    }

    @Override
    public void resume() {
        inputManager.reset();
    }

    @Override
    public void destroy() {
        if (mouseInput != null) {
            mouseInput.destroy();
        }

        if (keyInput != null) {
            keyInput.destroy();
        }

        if (touchInput != null) {
            touchInput.destroy();
        }
    }

    @Override
    public void initialize() {
        if (mouseInput != null) {
            mouseInput.initialize();
        }

        if (keyInput != null) {
            keyInput.initialize();
        }

        if (touchInput != null) {
            touchInput.initialize();
        }
    }

    @Override
    public void update(final float timePerFrame) {
        inputManager.update(timePerFrame);
    }

    @Override
    protected void entityAdded(final Interactive entity) {
        for (final PlayerInput input : entity.handledInputs()) {
            final InputActionListener listener =
                    new InputActionListener(entity, input);
            listeners.put(entity, listener);
            inputManager.addListener(listener, input.getName());
        }
    }

    @Override
    protected void entityRemoved(final Interactive entity) {
        for (final InputListener listener : listeners.get(entity)) {
            inputManager.removeListener(listener);
        }
        listeners.removeAll(entity);
    }

    private static class InputActionListener implements ActionListener {
        private final Interactive entity;
        private final PlayerInput input;

        public InputActionListener(final Interactive entity,
                final PlayerInput input) {
            this.entity = entity;
            this.input = input;
        }

        @Override
        public void onAction(final String name, final boolean isPressed,
                final float tpf) {
            if (input.getName().equals(name)) {
                entity.handleInput(input, isPressed);
            }
        }

    }
}
