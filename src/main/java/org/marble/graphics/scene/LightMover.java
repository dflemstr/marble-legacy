package org.marble.graphics.scene;

import com.ardor3d.light.PointLight;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyEventListener;
import com.ardor3d.scenegraph.event.DirtyType;

public final class LightMover implements DirtyEventListener {
    private final Vector3 lightOffset = new Vector3();
    private final Vector3 tempPos = new Vector3();
    private final Spatial spatial;
    private final PointLight light;

    public ReadOnlyVector3 getLightOffset() {
        return lightOffset;
    }

    public void setLightOffset(final ReadOnlyVector3 lightOffset) {
        this.lightOffset.set(lightOffset);
    }

    public LightMover(final Spatial spatial, final PointLight light) {
        this.spatial = spatial;
        this.light = light;
    }

    @Override
    public boolean
            spatialDirty(final Spatial spatial, final DirtyType dirtyType) {
        return false;
    }

    @Override
    public boolean
            spatialClean(final Spatial spatial, final DirtyType dirtyType) {
        if (this.spatial == spatial && dirtyType == DirtyType.Transform) {
            tempPos.set(spatial.getWorldTranslation());
            tempPos.addLocal(lightOffset);
            light.setLocation(tempPos);
        }
        return false;
    }
}
