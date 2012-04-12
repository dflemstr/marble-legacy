package org.marble.graphics.scene;

import com.ardor3d.scenegraph.Spatial;

public class PreDrawingVisitor extends AbstractRenderVisitor {

    @Override
    public void visit(final Spatial spatial) {
        if (spatial instanceof PreDrawing) {
            ((PreDrawing) spatial).preDraw(getRenderer());
        }
    }

}
