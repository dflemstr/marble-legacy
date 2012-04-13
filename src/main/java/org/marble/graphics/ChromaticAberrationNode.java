package org.marble.graphics;

import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.scenegraph.Spatial;

import org.marble.util.Shaders;

public class ChromaticAberrationNode extends EnvironmentNode {
    public ChromaticAberrationNode(final Spatial root,
            final ReadOnlyColorRGBA environmentColor,
            final int textureSizeMagnitude) {
        super(root, environmentColor, createChromaticAberrationShader(),
                textureSizeMagnitude);
    }

    private static GLSLShaderObjectsState createChromaticAberrationShader() {
        final GLSLShaderObjectsState shader =
                Shaders.loadShader("chromatic-aberration");

        // IOR values for Borosilicate Crown Glass, unrealistic?
        // shader.setUniform("etaR", 1 / 1.50917f);
        // shader.setUniform("etaG", 1 / 1.51534f);
        // shader.setUniform("etaB", 1 / 1.52136f);

        // Non-convex IOR values, making the refracted object look "hollow"
        shader.setUniform("etaR", 1.14f);
        shader.setUniform("etaG", 1.12f);
        shader.setUniform("etaB", 1.10f);

        shader.setUniform("fresnelPower", 2.0f);

        return shader;
    }

}
