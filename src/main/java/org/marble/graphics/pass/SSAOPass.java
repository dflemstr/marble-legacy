package org.marble.graphics.pass;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.ColorRGBA;
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

import org.marble.graphics.shader.FrustumSpaceDataLogic;
import org.marble.graphics.shader.ScreenSpaceDataLogic;
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

    private final DisplaySettings displaySettings;

    private float sampleRadius = 1.0f;
    private float intensity = 8.0f;
    private float scale = 1.0f;
    private float bias = 0.1f;
    private float cutoff = 0.99f;
    private boolean showOnlyAO = false;
    private boolean disableBlur = false;

    public SSAOPass(final DisplaySettings displaySettings,
            final Texture2D depthTexture, final Texture2D normalTexture,
            final int downsamples) {
        this.downsamples = downsamples;
        this.displaySettings = displaySettings;

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
        ssaoShader.setShaderDataLogic(new FrustumSpaceDataLogic());

        blurShader = Shaders.loadShader("ssao-blur");
        blurShader.setUniform("screen", 0);
        blurShader.setUniform("depth", 1);
        blurShader.setUniform("ssao", 2);
        blurShader.setShaderDataLogic(new ScreenSpaceDataLogic());

        writeUniforms();
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

    /**
     * @return the bias
     */
    public float getBias() {
        return bias;
    }

    /**
     * @return the cutoff
     */
    public float getCutoff() {
        return cutoff;
    }

    /**
     * @return the intensity
     */
    public float getIntensity() {
        return intensity;
    }

    /**
     * @return the sample radius
     */
    public float getSampleRadius() {
        return sampleRadius;
    }

    /**
     * @return the scale
     */
    public float getScale() {
        return scale;
    }

    /**
     * @param bias
     *            the bias to set
     */
    public void setBias(final float bias) {
        this.bias = bias;
        ssaoShader.setUniform("bias", bias);
    }

    /**
     * @param cutoff
     *            the cutoff to set
     */
    public void setCutoff(final float cutoff) {
        this.cutoff = cutoff;
        ssaoShader.setUniform("cutoff", cutoff);
    }

    /**
     * @param disableBlur
     *            whether blur should be disabled
     */
    public void setDisableBlur(final boolean disableBlur) {
        this.disableBlur = disableBlur;
        blurShader.setUniform("disableBlur", disableBlur);
    }

    /**
     * @param intensity
     *            the intensity to set
     */
    public void setIntensity(final float intensity) {
        this.intensity = intensity;
        ssaoShader.setUniform("intensity", intensity);
    }

    /**
     * @param sampleRadius
     *            the sample radius to set
     */
    public void setSampleRadius(final float sampleRadius) {
        this.sampleRadius = sampleRadius;
        ssaoShader.setUniform("sampleRadius", sampleRadius);
    }

    /**
     * @param scale
     *            the scale to set
     */
    public void setScale(final float scale) {
        this.scale = scale;
        ssaoShader.setUniform("scale", scale);
    }

    /**
     * @param showOnlyAO
     *            whether to only render the ambient occlusion without tinting
     */
    public void setShowOnlyAO(final boolean showOnlyAO) {
        this.showOnlyAO = showOnlyAO;
        blurShader.setUniform("showOnlyAO", showOnlyAO);
    }

    /**
     * @return whether blur should be disabled
     */
    public boolean shouldDisableBlur() {
        return disableBlur;
    }

    /**
     * @return whether to only render the ambient occlusion without tinting
     */
    public boolean shouldShowOnlyAO() {
        return showOnlyAO;
    }

    private void ensurePassRenderer(final Renderer r) {
        if (screenRenderer == null) {
            screenRenderer =
                    TextureRendererFactory.INSTANCE.createTextureRenderer(
                            displaySettings, false, r, ContextManager
                                    .getCurrentContext().getCapabilities());
            screenRenderer.setBackgroundColor(new ColorRGBA(0.0f, 0.0f, 0.0f,
                    0.0f));
            screenRenderer.setupTexture(screenTexture);
        }
        if (ssaoRenderer == null) {
            final DisplaySettings settings =
                    new DisplaySettings(displaySettings.getWidth()
                            / downsamples, displaySettings.getHeight()
                            / downsamples, displaySettings.getColorDepth(),
                            displaySettings.getFrequency(),
                            displaySettings.getAlphaBits(),
                            displaySettings.getDepthBits(),
                            displaySettings.getStencilBits(),
                            displaySettings.getSamples(),
                            displaySettings.isFullScreen(),
                            displaySettings.isStereo());
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

    private void writeUniforms() {
        ssaoShader.setUniform("sampleRadius", sampleRadius);
        ssaoShader.setUniform("intensity", intensity);
        ssaoShader.setUniform("scale", scale);
        ssaoShader.setUniform("bias", bias);
        ssaoShader.setUniform("cutoff", cutoff);

        blurShader.setUniform("showOnlyAO", showOnlyAO);
    }

    @Override
    protected void doRender(final Renderer r) {
        ensurePassRenderer(r);

        final Camera cam = Camera.getCurrentCamera();
        screenRenderer.copyToTexture(screenTexture, 0, 0, cam.getWidth(),
                cam.getHeight(), 0, 0);

        fullScreenQuad.setRenderState(ssaoTextureState);
        fullScreenQuad.setRenderState(ssaoShader);
        fullScreenQuad.updateWorldRenderStates(false);

        ssaoRenderer.render(fullScreenQuad, ssaoTexture,
                Renderer.BUFFER_COLOR_AND_DEPTH);

        fullScreenQuad.setRenderState(blurTextureState);
        fullScreenQuad.setRenderState(blurShader);
        fullScreenQuad.updateWorldRenderStates(false);

        r.draw((Renderable) fullScreenQuad);
    }
}
