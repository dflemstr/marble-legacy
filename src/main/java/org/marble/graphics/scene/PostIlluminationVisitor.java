package org.marble.graphics.scene;

import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;

public class PostIlluminationVisitor extends AbstractRenderVisitor {

    @Override
    boolean shouldRender(final Spatial spatial) {
        return spatial instanceof PostIllumination;
    }

    @Override
    void render(final Spatial spatial, final Renderer renderer) {
        ((PostIllumination) spatial).postIllumination(renderer);
    }

}
