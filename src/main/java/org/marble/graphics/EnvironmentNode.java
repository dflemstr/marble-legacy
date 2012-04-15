package org.marble.graphics;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.image.Texture.EnvironmentalMapMode;
import com.ardor3d.image.TextureCubeMap;
import com.ardor3d.image.TextureCubeMap.Face;
import com.ardor3d.image.TextureStoreFormat;
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
import com.ardor3d.renderer.state.ClipState;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;

import org.marble.graphics.scene.PreDrawing;
import org.marble.graphics.shader.WorldSpaceDataLogic;

/**
 * A graphical node that manages a shader, which applies a material dependent on
 * a cube map environment. This node takes care of rendering that cube map
 * environment and keeping the shader up-to-date with the environment.
 * 
 * All of the subnodes to this node will have the shader applied by default. The
 * environment will be adjusted accordingly to match the location of each
 * sub-node.
 */
public class EnvironmentNode extends Node implements PreDrawing {
    // Marks whether we are rendering an environment
    private static final AtomicBoolean renderingToEnv =
            new AtomicBoolean(false);

    // The root of the scene that we should reflect
    protected final Spatial root;

    // The color of the environment; if the surrounding world contains infinite
    // space as seen from the location of this node, that space is filled by
    // this color
    protected final ColorRGBA environmentColor = new ColorRGBA();

    // The renderer responsible for rendering the environment
    protected TextureRenderer envRenderer;

    // The actual rendered cube map
    protected final TextureCubeMap environment;

    // The texture state responsible for applying the environment texture to
    // meshes that use the shader
    protected final TextureState textures;

    // The applied shader
    protected final GLSLShaderObjectsState shader;

    protected final int textureSizeMagnitude;

    private final GLSLShaderObjectsState emptyShader;
    private final TextureState emptyTexture;
    private final ClipState emptyClip;
    private final CullState cullBackFace;
    private final LightState emptyLights;

    /**
     * Creates a new environment node.
     * 
     * @param root
     *            The root of the scene to reflect. Only spatials that are
     *            children of this node will be rendered.
     * @param environmentColor
     *            The color to use for infinite space in the surrounding
     *            environment
     * @param shader
     *            A shader that accepts an environment cube map as an uniform.
     *            The shader may define three uniforms:
     *            {@code uniform samplerCube environment;} which is a cube map
     *            describing the currently rendered object's environment,
     *            {@code uniform vec3 cameraPos;} which describes where the
     *            current camera is in world-space, and
     *            {@code uniform mat4 modelMatrix;} which describes how to
     *            transform the current object's vertices to get from model
     *            space into world space.
     */
    public EnvironmentNode(final Spatial root,
            final ReadOnlyColorRGBA environmentColor,
            final GLSLShaderObjectsState shader, final int textureSizeMagnitude) {
        this.root = root;
        this.environmentColor.set(environmentColor);
        this.shader = shader;
        this.textureSizeMagnitude = textureSizeMagnitude;

        // Inform the shader that the environment will be stored in texture unit
        // 0
        shader.setUniform("environment", 0);
        // Make the shader get information about camera and entity location in
        // world coordinates
        shader.setShaderDataLogic(new WorldSpaceDataLogic());
        shader.setEnabled(true);

        environment = new TextureCubeMap();
        environment.setTextureStoreFormat(TextureStoreFormat.RGB8);
        environment.setEnvironmentalMapMode(EnvironmentalMapMode.ObjectLinear);

        textures = new TextureState();
        // Store the environment texture in texture unit 0
        textures.setTexture(environment, 0);
        textures.setEnabled(true);

        // Apply the states to this node and all sub-nodes
        setRenderState(textures);
        setRenderState(shader);

        emptyShader = new GLSLShaderObjectsState();
        emptyShader.setEnabled(false);

        emptyClip = new ClipState();
        emptyClip.setEnabled(false);

        emptyTexture = new TextureState();
        emptyTexture.setEnabled(false);

        cullBackFace = new CullState();
        cullBackFace.setCullFace(CullState.Face.Back);
        cullBackFace.setEnabled(true);

        emptyLights = new LightState();
        emptyLights.setEnabled(false);
    }

    @Override
    public void draw(final Renderer r) {
        ensureEnvironment(r);
        super.draw(r);
    }

    /**
     * The environment that is being rendered by this node.
     */
    public TextureCubeMap getEnvironment() {
        return environment;
    }

    @Override
    public void preDraw(final Renderer r) {
        ensureEnvironment(r);

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
            // Render the environment as seen from the center of our
            // node
            renderCam.setLocation(getWorldTranslation());

            renderCam.setAxes(Vector3.NEG_UNIT_Z, Vector3.NEG_UNIT_Y,
                    Vector3.NEG_UNIT_X);
            environment.setCurrentRTTFace(Face.NegativeX);
            envRenderer.render(root, environment,
                    Renderer.BUFFER_COLOR_AND_DEPTH);

            renderCam.setAxes(Vector3.UNIT_Z, Vector3.NEG_UNIT_Y,
                    Vector3.UNIT_X);
            environment.setCurrentRTTFace(Face.PositiveX);
            envRenderer.render(root, environment,
                    Renderer.BUFFER_COLOR_AND_DEPTH);

            renderCam.setAxes(Vector3.NEG_UNIT_X, Vector3.NEG_UNIT_Z,
                    Vector3.NEG_UNIT_Y);
            environment.setCurrentRTTFace(Face.NegativeY);
            envRenderer.render(root, environment,
                    Renderer.BUFFER_COLOR_AND_DEPTH);

            renderCam.setAxes(Vector3.NEG_UNIT_X, Vector3.UNIT_Z,
                    Vector3.UNIT_Y);
            environment.setCurrentRTTFace(Face.PositiveY);
            envRenderer.render(root, environment,
                    Renderer.BUFFER_COLOR_AND_DEPTH);

            renderCam.setAxes(Vector3.UNIT_X, Vector3.NEG_UNIT_Y,
                    Vector3.NEG_UNIT_Z);
            environment.setCurrentRTTFace(Face.NegativeZ);
            envRenderer.render(root, environment,
                    Renderer.BUFFER_COLOR_AND_DEPTH);

            renderCam.setAxes(Vector3.NEG_UNIT_X, Vector3.NEG_UNIT_Y,
                    Vector3.UNIT_Z);
            environment.setCurrentRTTFace(Face.PositiveZ);
            envRenderer.render(root, environment,
                    Renderer.BUFFER_COLOR_AND_DEPTH);

            // We are no longer rendering the environment.
            getSceneHints().setCullHint(CullHint.Dynamic);

            renderingToEnv.set(false);
        }

        // We might have been excluded from view because we changed our cull
        // hint, so we'll inform the frustum solver that we might be visible
        // again, by saying that we are at the edge of the view frustum
        setLastFrustumIntersection(FrustumIntersect.Intersects);
    }

    private void ensureEnvironment(final Renderer r) {
        if (envRenderer == null) {
            final ContextCapabilities caps =
                    ContextManager.getCurrentContext().getCapabilities();

            final DisplaySettings settings =
                    new DisplaySettings(1 << textureSizeMagnitude,
                            1 << textureSizeMagnitude, 24, 0, 0, 24, 0, 0,
                            false, false);
            envRenderer =
                    TextureRendererFactory.INSTANCE.createTextureRenderer(
                            settings, false, r, caps);
            envRenderer.setBackgroundColor(environmentColor);
            envRenderer.getCamera().setFrustum(0.0625, 1024, -0.0625, 0.0625,
                    0.0625, -0.0625);

            // envRenderer.enforceState(emptyTexture);
            envRenderer.enforceState(emptyClip);
            envRenderer.enforceState(cullBackFace);
            // envRenderer.enforceState(emptyLights);
            // envRenderer.enforceState(emptyShader);
            envRenderer.setupTexture(environment);
        }
    }

    @Override
    protected void finalize() {
        // XXX This might be called too late/never, but it's better than
        // nothing.
        envRenderer.cleanup();
    }
}
