package org.marble.graphics.shader;

import com.ardor3d.math.Vector2;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.GLSLShaderDataLogic;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.scenegraph.Mesh;

public class ScreenSpaceDataLogic implements GLSLShaderDataLogic {

    private final Vector2 resolution = new Vector2();

    @Override
    public void applyData(final GLSLShaderObjectsState shader, final Mesh mesh,
            final Renderer renderer) {
        final Camera cam = Camera.getCurrentCamera();
        resolution.set(cam.getWidth(), cam.getHeight());
        shader.setUniform("resolution", resolution);
        shader.setUniform("znear", (float) cam.getFrustumNear());
        shader.setUniform("zfar", (float) cam.getFrustumFar());
    }

}
