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
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Renderable;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Quad;

import org.marble.util.Shaders;

public class SSAOPass extends Pass {
    private static final long serialVersionUID = -2297868571357545271L;

    private final int downsamples;

    private TextureRenderer ssaoRenderer;
    private Quad fullScreenQuad;

    private TextureRenderer screenRenderer;
    private final Texture2D screenTexture;

    private final Texture2D ssaoTexture;
    private final TextureState ssaoTextureState;
    private final TextureState blurTextureState;
    private final GLSLShaderObjectsState ssaoShader;
    private final GLSLShaderObjectsState blurShader;

    private final Vector3 frustumCorner = new Vector3();
    private final Vector2 resolution = new Vector2();

    public SSAOPass(final Texture2D depthTexture,
            final Texture2D normalTexture, final int downsamples) {
        this.downsamples = downsamples;

        screenTexture = new Texture2D();
        screenTexture.setWrap(Texture.WrapMode.Clamp);
        screenTexture
                .setMagnificationFilter(Texture.MagnificationFilter.Bilinear);

        ssaoTexture = new Texture2D();
        ssaoTexture.setWrap(Texture.WrapMode.Clamp);
        ssaoTexture
                .setMagnificationFilter(Texture.MagnificationFilter.Bilinear);

        ssaoTextureState = new TextureState();
        ssaoTextureState.setTexture(screenTexture, 0);
        ssaoTextureState.setTexture(depthTexture, 1);
        ssaoTextureState.setTexture(normalTexture, 2);
        ssaoTextureState.setEnabled(true);

        blurTextureState = new TextureState();
        blurTextureState.setTexture(screenTexture, 0);
        blurTextureState.setTexture(depthTexture, 1);
        blurTextureState.setTexture(ssaoTexture, 2);
        blurTextureState.setEnabled(true);

        ssaoShader = Shaders.loadShader("ssao");
        ssaoShader.setUniform("screen", 0);
        ssaoShader.setUniform("depth", 1);
        ssaoShader.setUniform("normal", 2);

        blurShader = Shaders.loadShader("ssao-blur");
        ssaoShader.setUniform("screen", 0);
        blurShader.setUniform("depth", 1);
        blurShader.setUniform("ssao", 2);
    }

    private void ensurePassRenderer(final Renderer r) {
        if (screenRenderer == null) {
            final Camera cam = Camera.getCurrentCamera();
            final DisplaySettings settings =
                    new DisplaySettings(cam.getWidth(), cam.getHeight(), 24, 0,
                            0, 8, 0, 0, false, false);
            screenRenderer =
                    TextureRendererFactory.INSTANCE.createTextureRenderer(
                            settings, false, r, ContextManager
                                    .getCurrentContext().getCapabilities());
            screenRenderer.setBackgroundColor(new ColorRGBA(0.0f, 0.0f, 0.0f,
                    0.0f));
            screenRenderer.setupTexture(screenTexture);
        }
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
        if (screenRenderer != null) {
            screenRenderer.cleanup();
        }
        if (ssaoRenderer != null) {
            ssaoRenderer.cleanup();
        }
    }

    @Override
    protected void doRender(final Renderer r) {
        ensurePassRenderer(r);
        final Camera cam = Camera.getCurrentCamera();

        resolution.set(cam.getWidth(), cam.getHeight());

        final double farY =
                (cam.getFrustumTop() / cam.getFrustumNear())
                        * cam.getFrustumFar();
        final double farX =
                farY * ((double) cam.getWidth() / (double) cam.getHeight());
        frustumCorner.set(farX, farY, cam.getFrustumFar());

        screenRenderer.copyToTexture(screenTexture, 0, 0, cam.getWidth(),
                cam.getHeight(), 0, 0);

        ssaoShader.setUniform("resolution", resolution);
        ssaoShader.setUniform("znear", (float) cam.getFrustumNear());
        ssaoShader.setUniform("zfar", (float) cam.getFrustumFar());
        ssaoShader.setUniform("frustumCorner", frustumCorner);

        fullScreenQuad.setRenderState(ssaoTextureState);
        fullScreenQuad.setRenderState(ssaoShader);
        fullScreenQuad.updateWorldRenderStates(false);

        ssaoRenderer.render(fullScreenQuad, ssaoTexture,
                Renderer.BUFFER_COLOR_AND_DEPTH);

        blurShader.setUniform("resolution", resolution);
        blurShader.setUniform("znear", (float) cam.getFrustumNear());
        blurShader.setUniform("zfar", (float) cam.getFrustumFar());

        fullScreenQuad.setRenderState(blurTextureState);
        fullScreenQuad.setRenderState(blurShader);
        fullScreenQuad.updateWorldRenderStates(false);

        r.draw((Renderable) fullScreenQuad);
    }
}
