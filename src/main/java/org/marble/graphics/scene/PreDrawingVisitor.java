package org.marble.graphics.scene;

import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;

public class PreDrawingVisitor extends AbstractRenderVisitor {

    @Override
    boolean shouldRender(final Spatial spatial) {
        return spatial instanceof PreDrawing;
    }

    @Override
    void render(final Spatial spatial, final Renderer renderer) {
        ((PreDrawing) spatial).preDraw(renderer);
    }

}
