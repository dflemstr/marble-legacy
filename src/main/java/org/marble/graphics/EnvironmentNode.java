package org.marble.graphics;

import com.ardor3d.image.Texture.EnvironmentalMapMode;
import com.ardor3d.image.TextureCubeMap;
import com.ardor3d.image.TextureCubeMap.Face;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix4;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.FrustumIntersect;
import com.ardor3d.renderer.ContextCapabilities;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRenderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;

public class EnvironmentNode extends Node {

    private boolean initialized = false;

    protected final Spatial root;
    protected final ColorRGBA environmentColor = new ColorRGBA();

    protected TextureRenderer renderer;
    protected TextureCubeMap environment;

    protected CullState culling;

    protected TextureState textures;
    protected final GLSLShaderObjectsState shader;

    public EnvironmentNode(final Spatial root,
            final ReadOnlyColorRGBA environmentColor, final GLSLShaderObjectsState shader) {
        this.root = root;
        this.environmentColor.set(environmentColor);
        this.shader = shader;
    }

    private void applyMaterial(final Spatial spatial) {
        spatial.setRenderState(culling);
        spatial.setRenderState(textures);
        spatial.setRenderState(shader);
    }

    @Override
    public void draw(final Renderer r) {
        initialize(r);

        // This node isn't part of the refracted/reflected surroundings, so if
        // this method is being called while we're rendering to the environment
        // textures, we'll cull ourselves from the render.

        // We are now rendering the environment.
        getSceneHints().setCullHint(CullHint.Always);

        final Camera renderCam = renderer.getCamera();
        renderCam.setLocation(getWorldTranslation());

        // Render the environment as seen from the center of our node
        renderCam.setAxes(Vector3.NEG_UNIT_Z, Vector3.NEG_UNIT_Y,
                Vector3.NEG_UNIT_X);
        environment.setCurrentRTTFace(Face.NegativeX);
        renderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

        renderCam.setAxes(Vector3.UNIT_Z, Vector3.NEG_UNIT_Y, Vector3.UNIT_X);
        environment.setCurrentRTTFace(Face.PositiveX);
        renderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

        renderCam.setAxes(Vector3.NEG_UNIT_X, Vector3.NEG_UNIT_Z,
                Vector3.NEG_UNIT_Y);
        environment.setCurrentRTTFace(Face.NegativeY);
        renderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

        renderCam.setAxes(Vector3.NEG_UNIT_X, Vector3.UNIT_Z, Vector3.UNIT_Y);
        environment.setCurrentRTTFace(Face.PositiveY);
        renderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

        renderCam.setAxes(Vector3.UNIT_X, Vector3.NEG_UNIT_Y,
                Vector3.NEG_UNIT_Z);
        environment.setCurrentRTTFace(Face.NegativeZ);
        renderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

        renderCam.setAxes(Vector3.NEG_UNIT_X, Vector3.NEG_UNIT_Y,
                Vector3.UNIT_Z);
        environment.setCurrentRTTFace(Face.PositiveZ);
        renderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

        // We are no longer rendering the environment.
        getSceneHints().setCullHint(CullHint.Dynamic);

        setLastFrustumIntersection(FrustumIntersect.Inside);

        final Camera mainCamera = Camera.getCurrentCamera();
        shader.setUniform("cameraPos", mainCamera.getLocation());

        final Matrix4 modelMatrix = Matrix4.fetchTempInstance();
        getWorldTransform().getHomogeneousMatrix(modelMatrix);
        shader.setUniform("modelMatrix", modelMatrix, true);
        Matrix4.releaseTempInstance(modelMatrix);

        // Render subnodes
        super.draw(r);
    }

    public TextureCubeMap getEnvironment() {
        return environment;
    }

    private void initialize(final Renderer r) {
        if (initialized)
            return;

        final ContextCapabilities caps =
                ContextManager.getCurrentContext().getCapabilities();

        renderer =
                TextureRendererFactory.INSTANCE.createTextureRenderer(1024,
                        1024, r, caps);
        renderer.setBackgroundColor(environmentColor);
        renderer.getCamera().setFrustum(.1, 1024, -.1, .1, .1, -.1);

        environment = new TextureCubeMap();
        environment.setEnvironmentalMapMode(EnvironmentalMapMode.ObjectLinear);

        renderer.setupTexture(environment);

        culling = new CullState();
        culling.setCullFace(CullState.Face.None);
        culling.setEnabled(true);

        textures = new TextureState();
        textures.setTexture(environment, 0);
        textures.setEnabled(true);

        shader.setUniform("environment", 0);
        shader.setEnabled(true);

        applyMaterial(this);

        initialized = true;
    }
}
