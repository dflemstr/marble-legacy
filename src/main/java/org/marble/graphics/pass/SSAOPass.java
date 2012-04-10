package org.marble.graphics.pass;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
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
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Renderable;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Quad;

import org.marble.util.Shaders;

public class SSAOPass extends Pass {
    private static final long serialVersionUID = -2297868571357545271L;

    private final int downsamples;
    private final double sampleRadius;
    private final double intensity;
    private final double scale;
    private final double bias;

    private TextureRenderer ssaoRenderer;
    private Quad fullScreenQuad;

    private final Texture2D ssaoTexture;
    private final TextureState ssaoTextureState;
    private final TextureState blurTextureState;
    private final BlendState blurBlendState;
    private final GLSLShaderObjectsState ssaoShader;
    private final GLSLShaderObjectsState blurShader;

    private final Vector3 frustumCorner = new Vector3();
    private final Vector2[] samples = { new Vector2(1.0, 0.0),
            new Vector2(-1.0, 0.0), new Vector2(0.0, 1.0),
            new Vector2(0.0, -1.0) };
    private final Vector2 resolution = new Vector2();
    private final Vector2 blurScale = new Vector2();

    public SSAOPass(final Texture2D normalDepthTexture, final int downsamples,
            final double sampleRadius, final double intensity,
            final double scale, final double bias) {
        this.downsamples = downsamples;
        this.sampleRadius = sampleRadius;
        this.intensity = intensity;
        this.scale = scale;
        this.bias = bias;

        ssaoTexture = new Texture2D();
        ssaoTexture.setWrap(Texture.WrapMode.Clamp);
        ssaoTexture
                .setMagnificationFilter(Texture.MagnificationFilter.Bilinear);

        ssaoTextureState = new TextureState();
        ssaoTextureState.setTexture(normalDepthTexture, 0);
        ssaoTextureState.setEnabled(true);

        blurTextureState = new TextureState();
        blurTextureState.setTexture(ssaoTexture, 0);
        blurTextureState.setTexture(normalDepthTexture, 1);
        blurTextureState.setEnabled(true);

        blurBlendState = new BlendState();
        blurBlendState.setBlendEnabled(true);
        blurBlendState.setSourceFunction(BlendState.SourceFunction.Zero);
        blurBlendState
                .setDestinationFunction(BlendState.DestinationFunction.SourceColor);
        blurBlendState.setEnabled(true);

        ssaoShader = Shaders.loadShader("ssao");
        ssaoShader.setUniform("normalDepths", 0);

        blurShader = Shaders.loadShader("ssao-blur");
        blurShader.setUniform("ssao", 0);
        blurShader.setUniform("normalDepths", 1);
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
    }

    @Override
    protected void doRender(final Renderer r) {
        ensurePassRenderer(r);
        final Camera cam = Camera.getCurrentCamera();

        final double farY =
                (cam.getFrustumTop() / cam.getFrustumNear())
                        * cam.getFrustumFar();
        final double farX =
                farY * ((double) cam.getWidth() / (double) cam.getHeight());
        frustumCorner.set(farX, farY, cam.getFrustumFar());

        resolution.set(cam.getWidth(), cam.getHeight());

        blurScale.set(1.5 / cam.getWidth(), 1.5 / cam.getHeight());

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
