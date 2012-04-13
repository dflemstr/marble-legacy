package org.marble.graphics.shader;

import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.scenegraph.Mesh;

public class FrustumSpaceDataLogic extends ScreenSpaceDataLogic {
    private final Vector3 frustumCorner = new Vector3();

    @Override
    public void applyData(final GLSLShaderObjectsState shader, final Mesh mesh,
            final Renderer renderer) {
        final Camera cam = Camera.getCurrentCamera();

        final double farY =
                cam.getFrustumTop() / cam.getFrustumNear()
                        * cam.getFrustumFar();
        final double farX =
                farY * ((double) cam.getWidth() / (double) cam.getHeight());
        frustumCorner.set(farX, farY, cam.getFrustumFar());

        shader.setUniform("frustumCorner", frustumCorner);

        super.applyData(shader, mesh, renderer);
    }
}
