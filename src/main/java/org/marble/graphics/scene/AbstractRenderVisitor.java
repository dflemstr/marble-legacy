package org.marble.graphics.scene;

import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;

public abstract class AbstractRenderVisitor implements RenderVisitor {
    private Renderer renderer;

    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public void setRenderer(final Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void visit(final Spatial spatial) {
        if (shouldRender(spatial)) {
            final CullHint cm = spatial.getSceneHints().getCullHint();
            if (cm == CullHint.Always) {
                spatial.setLastFrustumIntersection(Camera.FrustumIntersect.Outside);
                return;
            } else if (cm == CullHint.Never) {
                spatial.setLastFrustumIntersection(Camera.FrustumIntersect.Intersects);
                render(spatial, getRenderer());
                return;
            }

            final Camera camera = Camera.getCurrentCamera();
            final int state = camera.getPlaneState();
            final Node parent = spatial.getParent();

            // check to see if we can cull this node
            spatial.setLastFrustumIntersection(((parent != null && spatial
                    .getParent().getWorldBound() != null) ? parent
                    .getLastFrustumIntersection()
                    : Camera.FrustumIntersect.Intersects));

            if (cm == CullHint.Dynamic
                    && spatial.getLastFrustumIntersection() == Camera.FrustumIntersect.Intersects) {
                spatial.setLastFrustumIntersection(camera.contains(spatial
                        .getWorldBound()));
            }

            if (spatial.getLastFrustumIntersection() != Camera.FrustumIntersect.Outside) {
                render(spatial, getRenderer());
            }
            camera.setPlaneState(state);
        }
    }

    abstract boolean shouldRender(Spatial spatial);

    abstract void render(Spatial spatial, Renderer renderer);
}
