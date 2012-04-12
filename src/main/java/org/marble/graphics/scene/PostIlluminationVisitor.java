package org.marble.graphics.scene;

import com.ardor3d.scenegraph.Spatial;

public class PostIlluminationVisitor extends AbstractRenderVisitor {

    @Override
    public void visit(final Spatial spatial) {
        if (spatial instanceof PostIllumination) {
            ((PostIllumination) spatial).postIllumination(getRenderer());
        }
    }

}
