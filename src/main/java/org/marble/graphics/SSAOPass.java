package org.marble.graphics;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.WrapMode;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRenderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.pass.Pass;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.ClipState;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Renderable;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.TextureManager;

import org.marble.util.Shaders;

public class SSAOPass extends Pass {
    private static final long serialVersionUID = -2297868571357545271L;

    private final Spatial rootNode;
    private final int downsamples;
    private final double sampleRadius;
    private final double intensity;
    private final double scale;
    private final double bias;

    private TextureRenderer ssaoRenderer;
    private TextureRenderer normalRenderer;
    private Quad fullScreenQuad;

    private final Texture2D normalTexture;
    private final Texture randomTexture;
    private final Texture2D ssaoTexture;
    private final TextureState ssaoTextureState;
    private final TextureState blurTextureState;
    private final BlendState blurBlendState;
    private final GLSLShaderObjectsState normalShader;
    private final GLSLShaderObjectsState ssaoShader;
    private final GLSLShaderObjectsState blurShader;

    private final Vector3 frustumCorner = new Vector3();
    private final Vector2[] samples = { new Vector2(1.0, 0.0),
            new Vector2(-1.0, 0.0), new Vector2(0.0, 1.0),
            new Vector2(0.0, -1.0) };
    private final Vector2 resolution = new Vector2();
    private final Vector2 blurScale = new Vector2();

    private final TextureState emptyTexture;
    private final ClipState emptyClip;
    private final CullState cullBackFace;
    private final LightState emptyLights;

    public SSAOPass(final Spatial rootNode, final int downsamples,
            final double sampleRadius, final double intensity,
            final double scale, final double bias) {
        this.rootNode = rootNode;
        this.downsamples = downsamples;
        this.sampleRadius = sampleRadius;
        this.intensity = intensity;
        this.scale = scale;
        this.bias = bias;

        normalTexture = new Texture2D();
        normalTexture.setTextureStoreFormat(TextureStoreFormat.RGBA16);
        normalTexture.setWrap(Texture.WrapMode.Clamp);
        normalTexture
                .setMagnificationFilter(Texture.MagnificationFilter.Bilinear);

        randomTexture =
                TextureManager.load("random.png",
                        Texture.MinificationFilter.BilinearNoMipMaps, false);
        randomTexture.setWrap(WrapMode.Repeat);

        ssaoTexture = new Texture2D();
        ssaoTexture.setWrap(Texture.WrapMode.Clamp);
        ssaoTexture
                .setMagnificationFilter(Texture.MagnificationFilter.Bilinear);

        ssaoTextureState = new TextureState();
        ssaoTextureState.setTexture(normalTexture, 0);
        ssaoTextureState.setTexture(randomTexture, 1);
        ssaoTextureState.setEnabled(true);

        blurTextureState = new TextureState();
        blurTextureState.setTexture(ssaoTexture, 0);
        blurTextureState.setTexture(normalTexture, 1);
        blurTextureState.setEnabled(true);

        emptyClip = new ClipState();
        emptyClip.setEnabled(false);

        emptyTexture = new TextureState();
        emptyTexture.setEnabled(false);

        cullBackFace = new CullState();
        cullBackFace.setCullFace(CullState.Face.Back);
        cullBackFace.setEnabled(true);

        emptyLights = new LightState();
        emptyLights.setEnabled(false);

        blurBlendState = new BlendState();
        blurBlendState.setBlendEnabled(true);
        blurBlendState.setSourceFunction(BlendState.SourceFunction.Zero);
        blurBlendState
                .setDestinationFunction(BlendState.DestinationFunction.SourceColor);
        blurBlendState.setEnabled(true);

        normalShader = Shaders.loadShader("normal");

        ssaoShader = Shaders.loadShader("ssao");
        ssaoShader.setUniform("normals", 0);
        ssaoShader.setUniform("randoms", 1);

        blurShader = Shaders.loadShader("ssao-blur");
        blurShader.setUniform("ssao", 0);
        blurShader.setUniform("normals", 1);
    }

    private void ensurePassRenderer(final Renderer r) {
        if (ssaoRenderer == null) {
            final Camera cam = Camera.getCurrentCamera();
            final DisplaySettings settings =
                    new DisplaySettings(cam.getWidth() / downsamples,
                            cam.getHeight() / downsamples, 24, 0, 0, 8, 0, 0,
                            false, false);
            ssaoRenderer =
                    TextureRendererFactory.INSTANCE.createTextureRenderer(
                            settings, false, r, ContextManager
                                    .getCurrentContext().getCapabilities());
            ssaoRenderer.setBackgroundColor(new ColorRGBA(0.0f, 0.0f, 0.0f,
                    0.0f));
            ssaoRenderer.setupTexture(ssaoTexture);
        }
        if (normalRenderer == null) {
            final Camera cam = Camera.getCurrentCamera();
            normalRenderer =
                    TextureRendererFactory.INSTANCE.createTextureRenderer(cam
                            .getWidth(), cam.getHeight(), r, ContextManager
                            .getCurrentContext().getCapabilities());
            normalRenderer.setBackgroundColor(new ColorRGBA(0.0f, 0.0f, 0.0f,
                    0.0f));
            normalRenderer.getCamera().setFrustum(cam.getFrustumNear(),
                    cam.getFrustumFar(), cam.getFrustumLeft(),
                    cam.getFrustumRight(), cam.getFrustumTop(),
                    cam.getFrustumBottom());
            normalRenderer.setupTexture(normalTexture);
        }
        if (fullScreenQuad == null) {
            final Camera cam = Camera.getCurrentCamera();
            fullScreenQuad =
                    new Quad("FullScreenQuad", cam.getWidth() / 4,
                            cam.getHeight() / 4);
            fullScreenQuad.setTranslation(cam.getWidth() / 2,
                    cam.getHeight() / 2, 0);
            fullScreenQuad.getSceneHints().setRenderBucketType(
                    RenderBucketType.Ortho);

            fullScreenQuad.getSceneHints().setCullHint(CullHint.Never);

            fullScreenQuad.updateGeometricState(0.0f, true);
        }
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (ssaoRenderer != null) {
            ssaoRenderer.cleanup();
        }
        if (normalRenderer != null) {
            normalRenderer.cleanup();
        }
    }

    @Override
    protected void doRender(final Renderer r) {
        ensurePassRenderer(r);
        final Camera cam = Camera.getCurrentCamera();

        normalRenderer.getCamera().setLocation(cam.getLocation());
        normalRenderer.getCamera().setDirection(cam.getDirection());
        normalRenderer.getCamera().setUp(cam.getUp());
        normalRenderer.getCamera().setLeft(cam.getLeft());

        final double farY =
                (cam.getFrustumTop() / cam.getFrustumNear())
                        * cam.getFrustumFar();
        final double farX =
                farY * ((double) cam.getWidth() / (double) cam.getHeight());
        frustumCorner.set(farX, farY, cam.getFrustumFar());

        resolution.set(cam.getWidth(), cam.getHeight());

        blurScale.set(1.5 / cam.getWidth(), 1.5 / cam.getHeight());

        normalRenderer.enforceState(emptyTexture);
        normalRenderer.enforceState(emptyClip);
        normalRenderer.enforceState(cullBackFace);
        normalRenderer.enforceState(emptyLights);
        normalRenderer.enforceState(normalShader);
        normalRenderer.render(rootNode, normalTexture,
                Renderer.BUFFER_COLOR_AND_DEPTH);
        normalRenderer.clearEnforcedStates();

        ssaoShader.setUniform("resolution", resolution);
        ssaoShader.setUniform("frustumCorner", frustumCorner);
        ssaoShader.setUniform("frustumNear", (float) cam.getFrustumNear());
        ssaoShader.setUniform("frustumFar", (float) cam.getFrustumFar());
        ssaoShader.setUniform("sampleRadius", (float) sampleRadius);
        ssaoShader.setUniform("intensity", (float) intensity);
        ssaoShader.setUniform("scale", (float) scale);
        ssaoShader.setUniform("bias", (float) bias);
        ssaoShader.setUniform("samples[0]", samples[0]);
        ssaoShader.setUniform("samples[1]", samples[1]);
        ssaoShader.setUniform("samples[2]", samples[2]);
        ssaoShader.setUniform("samples[3]", samples[3]);

        fullScreenQuad.setRenderState(ssaoTextureState);
        fullScreenQuad.setRenderState(ssaoShader);
        fullScreenQuad.clearRenderState(StateType.Blend);
        fullScreenQuad.updateWorldRenderStates(false);

        ssaoRenderer.render(fullScreenQuad, ssaoTexture,
                Renderer.BUFFER_COLOR_AND_DEPTH);

        blurShader.setUniform("frustumNear", (float) cam.getFrustumNear());
        blurShader.setUniform("frustumFar", (float) cam.getFrustumFar());
        blurShader.setUniform("scale", blurScale);

        fullScreenQuad.setRenderState(blurTextureState);
        fullScreenQuad.setRenderState(blurBlendState);
        fullScreenQuad.setRenderState(blurShader);
        fullScreenQuad.updateWorldRenderStates(false);

        r.draw((Renderable) fullScreenQuad);

    }
}
