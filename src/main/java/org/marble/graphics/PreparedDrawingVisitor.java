package org.marble.graphics;

import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.visitor.Visitor;

public class PreparedDrawingVisitor implements Visitor {
    private final Renderer renderer;

    public PreparedDrawingVisitor(final Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void visit(final Spatial spatial) {
        if (spatial instanceof PreparedDrawing) {
            ((PreparedDrawing) spatial).preDraw(renderer);
        }
    }

}
