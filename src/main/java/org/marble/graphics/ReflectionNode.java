package org.marble.graphics;

import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.scenegraph.Spatial;

import org.marble.util.Shaders;

public class ReflectionNode extends EnvironmentNode {

    private static GLSLShaderObjectsState createReflectionShader() {
        return Shaders.loadShader("reflection");
    }

    public ReflectionNode(final Spatial root,
            final ReadOnlyColorRGBA environmentColor) {
        super(root, environmentColor, createReflectionShader());
    }

}
