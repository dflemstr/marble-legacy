package org.marble.util;

import java.io.IOException;
import java.util.Map;

import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.util.resource.ResourceLocatorTool;

import com.google.common.collect.Maps;

public final class Shaders {
    private static final Map<String, Integer> shaderLoadCount = Maps
            .newHashMap();

    private Shaders() {
    }

    public static GLSLShaderObjectsState loadShader(final String name) {
        final GLSLShaderObjectsState shader = new GLSLShaderObjectsState();

        try {
            shader.setVertexShader(ResourceLocatorTool.locateResource(
                    ResourceLocatorTool.TYPE_SHADER, name + ".vert")
                    .openStream());
            shader.setFragmentShader(ResourceLocatorTool.locateResource(
                    ResourceLocatorTool.TYPE_SHADER, name + ".frag")
                    .openStream());
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
}
