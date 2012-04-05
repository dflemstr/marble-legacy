package org.marble.graphics;

import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.GLSLShaderDataLogic;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Mesh;

public class LightDataLogic implements GLSLShaderDataLogic {

    @Override
    public void applyData(final GLSLShaderObjectsState shader, final Mesh mesh,
            final Renderer renderer) {
        final RenderContext context = ContextManager.getCurrentContext();
        final LightState light =
                (LightState) context.getCurrentState(StateType.Light);
        shader.setUniform("lightCount", light.getLightList().size());
    }
}
