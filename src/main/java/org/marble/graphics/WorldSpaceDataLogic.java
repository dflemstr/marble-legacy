package org.marble.graphics;

import com.ardor3d.math.Matrix4;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.GLSLShaderDataLogic;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.scenegraph.Mesh;

/**
 * Data logic that provides a shader with information about the current camera
 * position, and the model matrix that is being used for rendering.
 */
final class WorldSpaceDataLogic implements GLSLShaderDataLogic {
    @Override
    public void applyData(final GLSLShaderObjectsState shader, final Mesh mesh,
            final Renderer renderer) {
        shader.setUniform("cameraPos", Camera.getCurrentCamera().getLocation());

        final Matrix4 modelMatrix = Matrix4.fetchTempInstance();
        mesh.getWorldTransform().getHomogeneousMatrix(modelMatrix);
        shader.setUniform("modelMatrix", modelMatrix, true);
        Matrix4.releaseTempInstance(modelMatrix);
    }
}
