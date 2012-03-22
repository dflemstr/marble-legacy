package org.marble.util;

import java.io.IOException;

import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.util.resource.ResourceLocatorTool;

import org.marble.Game;

public final class Shaders {
    private static final String SHADER_DIR = Game.class.getPackage().getName()
            .replace('.', '/')
            + "/media/shader/";

    public static GLSLShaderObjectsState loadShader(final String name) {
        final GLSLShaderObjectsState shader = new GLSLShaderObjectsState();

        try {
            shader.setVertexShader(ResourceLocatorTool
                    .getClassPathResourceAsStream(Shaders.class, SHADER_DIR
                            + name + ".vert"));
            shader.setFragmentShader(ResourceLocatorTool
                    .getClassPathResourceAsStream(Shaders.class, SHADER_DIR
                            + name + ".frag"));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return shader;
    }

    private Shaders() {
    }
}
