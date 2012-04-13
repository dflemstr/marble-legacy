package org.marble.graphics;

import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.scenegraph.Spatial;

import org.marble.util.Shaders;

public class ReflectionNode extends EnvironmentNode {

    public ReflectionNode(final Spatial root,
            final ReadOnlyColorRGBA environmentColor,
            final int textureSizeMagnitude) {
        super(root, environmentColor, createReflectionShader(),
                textureSizeMagnitude);
    }

    private static GLSLShaderObjectsState createReflectionShader() {
        return Shaders.loadShader("reflection");
    }

}
