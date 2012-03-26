package org.marble.util;

import java.io.IOException;
import java.util.Map;

import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.util.resource.ResourceLocatorTool;

import com.google.common.collect.Maps;

import org.marble.Game;

public final class Shaders {
    private static final String SHADER_DIR = Game.class.getPackage().getName()
            .replace('.', '/')
            + "/shader/";
    private static final Map<String, Integer> shaderLoadCount = Maps
            .newHashMap();

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

        int loadCount =
                shaderLoadCount.containsKey(name) ? shaderLoadCount.get(name)
                        : 0;
        loadCount++;

        shader._fragmentShaderName = name + loadCount;
        shader._vertexShaderName = name + loadCount;

        shaderLoadCount.put(name, loadCount);

        return shader;
    }

    private Shaders() {
    }
}
