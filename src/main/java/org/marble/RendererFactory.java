package org.marble;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.lwjgl.LwjglCanvas;
import com.ardor3d.framework.lwjgl.LwjglCanvasRenderer;
import com.ardor3d.input.ControllerWrapper;
import com.ardor3d.input.FocusWrapper;
import com.ardor3d.input.KeyboardWrapper;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.MouseWrapper;
import com.ardor3d.input.lwjgl.LwjglControllerWrapper;
import com.ardor3d.input.lwjgl.LwjglKeyboardWrapper;
import com.ardor3d.input.lwjgl.LwjglMouseManager;
import com.ardor3d.input.lwjgl.LwjglMouseWrapper;
import com.ardor3d.renderer.TextureRendererProvider;
import com.ardor3d.renderer.lwjgl.LwjglTextureRendererProvider;

/**
 * A renderer implementation. All of the renderer-specific code is provided via
 * the {@link #getFactory()} method.
 */
public enum RendererFactory {
    /** The LWJGL (Lightweight Java Game Library) renderer implementation */
    LWJGL() {
        @Override
        public NativeCanvas createCanvas(final DisplaySettings displaySettings,
                final Scene scene) {
            final LwjglCanvasRenderer canvasRenderer =
                    new LwjglCanvasRenderer(scene);
            return new LwjglCanvas(displaySettings, canvasRenderer);
        }

        @Override
        public ControllerWrapper createControllerWrapper() {
            return new LwjglControllerWrapper();
        }

        @Override
        public FocusWrapper createFocusWrapper(final NativeCanvas canvas) {
            return (LwjglCanvas) canvas;
        }

        @Override
        public KeyboardWrapper createKeyboardWrapper() {
            return new LwjglKeyboardWrapper();
        }

        @Override
        public MouseManager createMouseManager() {
            return new LwjglMouseManager();
        }

        @Override
        public MouseWrapper createMouseWrapper() {
            return new LwjglMouseWrapper();
        }

        @Override
        public TextureRendererProvider createTextureRendererProvider() {
            return new LwjglTextureRendererProvider();
        }
    };

    /**
     * Creates a canvas for a renderer implementation.
     * 
     * @param displaySettings
     *            The display settings that the canvas should be created with.
     * @param scene
     *            The scene that handles the rendering for the canvas.
     * @return The created canvas.
     */
    public abstract NativeCanvas createCanvas(DisplaySettings displaySettings,
            Scene scene);

    /**
     * Creates a controller wrapper for a renderer implementation.
     * 
     * @return A controller wrapper for a renderer implementation.
     */
    public abstract ControllerWrapper createControllerWrapper();

    /**
     * Creates a focus wrapper for a renderer implementation.
     * 
     * @param canvas
     *            The canvas that the focus should be controlled on.
     * @return A focus wrapper for a renderer implementation.
     */
    public abstract FocusWrapper createFocusWrapper(NativeCanvas canvas);

    /**
     * Creates a keyboard wrapper for a renderer implementation.
     * 
     * @return A keyboard wrapper for a renderer implementation.
     */
    public abstract KeyboardWrapper createKeyboardWrapper();

    /**
     * Creates a mouse manager for a renderer implementation.
     * 
     * @return A mouse manager for a renderer implementation.
     */
    public abstract MouseManager createMouseManager();

    /**
     * Creates a mouse wrapper for a renderer implementation.
     * 
     * @return A mouse wrapper for a renderer implementation.
     */
    public abstract MouseWrapper createMouseWrapper();

    /**
     * Creates a texture renderer provider for a renderer implementation.
     * 
     * @return A texture renderer provider for a renderer implementation.
     */
    public abstract TextureRendererProvider createTextureRendererProvider();
}
