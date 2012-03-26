package org.marble.graphics;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ardor3d.image.Texture.EnvironmentalMapMode;
import com.ardor3d.image.TextureCubeMap;
import com.ardor3d.image.TextureCubeMap.Face;
import com.ardor3d.math.ColorRGBA;
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
    private static final AtomicBoolean renderingToEnv =
            new AtomicBoolean(false);

    protected final Spatial root;
    protected final ColorRGBA environmentColor = new ColorRGBA();

    protected TextureRenderer envRenderer;
    protected final TextureCubeMap environment;

    protected final CullState culling;
    protected final TextureState textures;

    protected final GLSLShaderObjectsState shader;

    public EnvironmentNode(final Spatial root,
            final ReadOnlyColorRGBA environmentColor,
            final GLSLShaderObjectsState shader) {
        this.root = root;
        this.environmentColor.set(environmentColor);
        this.shader = shader;
        shader.setUniform("environment", 0);
        shader.setShaderDataLogic(new WorldSpaceDataLogic());
        shader.setEnabled(true);

        culling = new CullState();
        culling.setCullFace(CullState.Face.None);
        culling.setEnabled(true);

        environment = new TextureCubeMap();
        environment.setEnvironmentalMapMode(EnvironmentalMapMode.ObjectLinear);

        textures = new TextureState();
        textures.setTexture(environment, 0);
        textures.setEnabled(true);

        setRenderState(culling);
        setRenderState(textures);
        setRenderState(shader);
    }

    private TextureRenderer createEnvironmentRenderer(final Renderer r) {
        final ContextCapabilities caps =
                ContextManager.getCurrentContext().getCapabilities();

        final TextureRenderer renderer =
                TextureRendererFactory.INSTANCE.createTextureRenderer(128, 128,
                        r, caps);
        renderer.setBackgroundColor(environmentColor);
        renderer.getCamera().setFrustum(.1, 1024, -.1, .1, .1, -.1);
        renderer.setupTexture(environment);
        return renderer;
    }

    @Override
    public void draw(final Renderer r) {
        if(envRenderer == null)
            envRenderer = createEnvironmentRenderer(r);

        /*
         * We only allow one environment to be rendered at a time, because
         * otherwise environments will be recursively rendered into one another,
         * having a 6^n complexity
         */
        if (renderingToEnv.compareAndSet(false, true)) {
            // We are now rendering the environment.

            /*
             * This node isn't part of the refracted/reflected surroundings, so
             * if this method is being called while we're rendering to the
             * environment textures, we'll cull ourselves from the render.
             */
            getSceneHints().setCullHint(CullHint.Always);

            final Camera renderCam = envRenderer.getCamera();
            renderCam.setLocation(getWorldTranslation());

            // Render the environment as seen from the center of our
            // node
            renderCam.setAxes(Vector3.NEG_UNIT_Z, Vector3.NEG_UNIT_Y,
                    Vector3.NEG_UNIT_X);
            environment.setCurrentRTTFace(Face.NegativeX);
            envRenderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

            renderCam.setAxes(Vector3.UNIT_Z, Vector3.NEG_UNIT_Y,
                    Vector3.UNIT_X);
            environment.setCurrentRTTFace(Face.PositiveX);
            envRenderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

            renderCam.setAxes(Vector3.NEG_UNIT_X, Vector3.NEG_UNIT_Z,
                    Vector3.NEG_UNIT_Y);
            environment.setCurrentRTTFace(Face.NegativeY);
            envRenderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

            renderCam.setAxes(Vector3.NEG_UNIT_X, Vector3.UNIT_Z,
                    Vector3.UNIT_Y);
            environment.setCurrentRTTFace(Face.PositiveY);
            envRenderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

            renderCam.setAxes(Vector3.UNIT_X, Vector3.NEG_UNIT_Y,
                    Vector3.NEG_UNIT_Z);
            environment.setCurrentRTTFace(Face.NegativeZ);
            envRenderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

            renderCam.setAxes(Vector3.NEG_UNIT_X, Vector3.NEG_UNIT_Y,
                    Vector3.UNIT_Z);
            environment.setCurrentRTTFace(Face.PositiveZ);
            envRenderer.render(root, environment, Renderer.BUFFER_COLOR_AND_DEPTH);

            // We are no longer rendering the environment.
            getSceneHints().setCullHint(CullHint.Dynamic);

            renderingToEnv.set(false);
        }

        setLastFrustumIntersection(FrustumIntersect.Intersects);

        // Render subnodes
        super.draw(r);
    }

    public TextureCubeMap getEnvironment() {
        return environment;
    }
}
