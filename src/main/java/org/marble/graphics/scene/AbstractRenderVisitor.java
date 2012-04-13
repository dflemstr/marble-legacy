package org.marble.graphics.scene;

import com.ardor3d.renderer.Renderer;

public abstract class AbstractRenderVisitor implements RenderVisitor {
    private Renderer renderer;

    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public void setRenderer(final Renderer renderer) {
        this.renderer = renderer;
    }
}
