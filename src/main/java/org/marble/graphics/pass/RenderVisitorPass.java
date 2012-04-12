package org.marble.graphics.pass;

import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.pass.Pass;

import org.marble.graphics.scene.RenderVisitor;

public class RenderVisitorPass extends Pass {
    private static final long serialVersionUID = -1481101594430337027L;

    private final RenderVisitor visitor;
    private final boolean renderBuckets;

    public RenderVisitorPass(final RenderVisitor visitor,
            final boolean renderBuckets) {
        this.visitor = visitor;
        this.renderBuckets = renderBuckets;
    }

    @Override
    protected void doRender(final Renderer r) {
        visitor.setRenderer(r);
        for (int i = 0, sSize = _spatials.size(); i < sSize; i++) {
            _spatials.get(i).acceptVisitor(visitor, false);
        }

        if (renderBuckets) {
            r.renderBuckets();
        }
    }
}
