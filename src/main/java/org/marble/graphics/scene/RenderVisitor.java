package org.marble.graphics.scene;

import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.visitor.Visitor;

public interface RenderVisitor extends Visitor {
    public void setRenderer(Renderer renderer);
}
