package org.marble.graphics.pass;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.type.ReadOnlyVector2;
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

import org.marble.graphics.shader.ScreenSpaceDataLogic;
import org.marble.util.Shaders;

public class DepthOfFieldPass extends Pass {
    private static final long serialVersionUID = 161203858061648402L;

    private TextureRenderer dofRenderer;
    private Quad fullScreenQuad;
    private final Texture2D screenTexture;
    private final TextureState dofTextureState;
    private final GLSLShaderObjectsState dofShader;
    private final DisplaySettings displaySettings;

    private float focalDepth = 0.0f;
    private float focalLength = 40.0f;
    private float fstop = 1.4f;
    private boolean showFocus = false;
    private int samples = 3;
    private int rings = 3;
    private boolean manualDOF = false;
    private float nearDOFStart = 1.0f;
    private float nearDOFDistance = 2.0f;
    private float farDOFStart = 1.0f;
    private float farDOFDistance = 3.0f;
    private float coc = 0.03f;
    private boolean vignetting = true;
    private float vignettingOuterBorder = 1.3f;
    private float vignettingInnerBorder = 0.0f;
    private float vignettingFade = 22.0f;
    private boolean autoFocus = true;
    private final Vector2 focus = new Vector2(0.5, 0.5);
    private float maxBlur = 1.0f;
    private float threshold = 0.5f;
    private float gain = 25.0f;
    private float bias = 0.2f;
    private float fringe = 0.7f;
    private boolean noise = true;
    private float noiseDitherAmount = 0.0001f;
    private boolean depthBlur = true;
    private float depthBlurSize = 1.25f;
    private boolean pentagonBokeh = false;
    private float pentagonFeather = 0.4f;

    /**
     * Creates a depth-of-field pass that simulates a camera lens.
     * 
     * @param displaySettings
     *            The active display settings.
     * @param depthTexture
     *            A texture describing the scene's depth at every texel.
     */
    public DepthOfFieldPass(final DisplaySettings displaySettings,
            final Texture2D depthTexture) {
        this.displaySettings = displaySettings;

        screenTexture = new Texture2D();
        screenTexture.setWrap(Texture.WrapMode.Clamp);
        screenTexture
                .setMagnificationFilter(Texture.MagnificationFilter.Bilinear);

        dofTextureState = new TextureState();
        dofTextureState.setTexture(screenTexture, 0);
        dofTextureState.setTexture(depthTexture, 1);

        dofShader = Shaders.loadShader("depth-of-field");
        dofShader.setUniform("screen", 0);
        dofShader.setUniform("depth", 1);
        dofShader.setShaderDataLogic(new ScreenSpaceDataLogic());
        writeUniforms();
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (dofRenderer != null) {
            dofRenderer.cleanup();
        }
    }

    /**
     * @return whether to show debug focus point and focal range (red = focal
     *         point, green = focal range)
     */
    public boolean doesShowFocus() {
        return showFocus;
    }

    /**
     * @return the bokeh edge bias
     */
    public float getBias() {
        return bias;
    }

    /**
     * @return the circle of confusion size in mm (35mm film = 0.03mm)
     */
    public float getCoC() {
        return coc;
    }

    /**
     * @return the depth blur size
     */
    public float getDepthBlurSize() {
        return depthBlurSize;
    }

    /**
     * @return the how far the depth-of-field blur reaches from the far start.
     */
    public float getFarDOFDistance() {
        return farDOFDistance;
    }

    /**
     * @return where the depth-of-field blur starts away from the camera.
     */
    public float getFarDOFStart() {
        return farDOFStart;
    }

    /**
     * @return the focal distance value in meters, but you may use the autoFocus
     *         option instead
     */
    public float getFocalDepth() {
        return focalDepth;
    }

    /**
     * @return the focal length in mm
     */
    public float getFocalLength() {
        return focalLength;
    }

    /**
     * @return auto focus point on screen (0.0,0.0 - left lower corner, 1.0,1.0
     *         - upper right)
     */
    public ReadOnlyVector2 getFocus() {
        return focus;
    }

    /**
     * @return the bokeh chromatic aberration/fringing
     */
    public float getFringe() {
        return fringe;
    }

    /**
     * @return the f-stop value
     */
    public float getFstop() {
        return fstop;
    }

    /**
     * @return the highlight gain
     */
    public float getGain() {
        return gain;
    }

    /**
     * @return clamp value of max blur (0.0 = no blur,1.0 default)
     */
    public float getMaxBlur() {
        return maxBlur;
    }

    /**
     * @return how far the depth-of-field blur reaches from the near start.
     */
    public float getNearDOFDistance() {
        return nearDOFDistance;
    }

    /**
     * @return where the depth-of-field blur starts towards the camera.
     */
    public float getNearDOFStart() {
        return nearDOFStart;
    }

    /**
     * @return the dither amount
     */
    public float getNoiseDitherAmount() {
        return noiseDitherAmount;
    }

    /**
     * @return the pentagon shape feather
     */
    public float getPentagonFeather() {
        return pentagonFeather;
    }

    /**
     * @return the ring count
     */
    public int getRings() {
        return rings;
    }

    /**
     * @return the samples on the first ring
     */
    public int getSamples() {
        return samples;
    }

    /**
     * @return the highlight threshold
     */
    public float getThreshold() {
        return threshold;
    }

    /**
     * @return f-stops till vignette fades
     */
    public float getVignettingFade() {
        return vignettingFade;
    }

    /**
     * @return the vignetting inner border
     */
    public float getVignettingInnerBorder() {
        return vignettingInnerBorder;
    }

    /**
     * @return the vignetting outer border
     */
    public float getVignettingOuterBorder() {
        return vignettingOuterBorder;
    }

    /**
     * @param autoFocus
     *            whether to use autoFocus in shader. Disable if you use an
     *            external focalDepth value.
     */
    public void setAutoFocus(final boolean autoFocus) {
        this.autoFocus = autoFocus;
        dofShader.setUniform("autoFocus", autoFocus);
    }

    /**
     * @param bias
     *            the bokeh edge bias to set
     */
    public void setBias(final float bias) {
        this.bias = bias;
        dofShader.setUniform("bias", bias);
    }

    /**
     * @param coc
     *            the circle of confusion size in mm (35mm film = 0.03mm)
     */
    public void setCoC(final float coc) {
        this.coc = coc;
        dofShader.setUniform("coc", coc);
    }

    /**
     * @param depthBlur
     *            whether to blur the depth buffer
     */
    public void setDepthBlur(final boolean depthBlur) {
        this.depthBlur = depthBlur;
        dofShader.setUniform("depthBlur", depthBlur);
    }

    /**
     * @param depthBlurSize
     *            the depth blur size to set
     */
    public void setDepthBlurSize(final float depthBlurSize) {
        this.depthBlurSize = depthBlurSize;
        dofShader.setUniform("depthBlurSize", depthBlurSize);
    }

    /**
     * @param farDOFDistance
     *            the how far the depth-of-field blur reaches from the far
     *            start.
     */
    public void setFarDOFDistance(final float farDOFDistance) {
        this.farDOFDistance = farDOFDistance;
        dofShader.setUniform("farDOFDistance", farDOFDistance);
    }

    /**
     * @param farDOFStart
     *            where the depth-of-field blur starts away from the camera.
     */
    public void setFarDOFStart(final float farDOFStart) {
        this.farDOFStart = farDOFStart;
        dofShader.setUniform("farDOFStart", farDOFStart);
    }

    /**
     * @param focalDepth
     *            the the focal distance value in meters to set
     */
    public void setFocalDepth(final float focalDepth) {
        this.focalDepth = focalDepth;
        dofShader.setUniform("focalDepth", focalDepth);
    }

    /**
     * @param focalLength
     *            the focal length in mm to set
     */
    public void setFocalLength(final float focalLength) {
        this.focalLength = focalLength;
        dofShader.setUniform("focalLength", focalLength);
    }

    /**
     * @param focus
     *            auto focus point on screen (0.0,0.0 - left lower corner,
     *            1.0,1.0 - upper right)
     */
    public void setFocus(final ReadOnlyVector2 focus) {
        this.focus.set(focus);
        dofShader.setUniform("focus", focus);
    }

    /**
     * @param fringe
     *            the bokeh chromatic aberration/fringing to set
     */
    public void setFringe(final float fringe) {
        this.fringe = fringe;
        dofShader.setUniform("fringe", fringe);
    }

    /**
     * @param fstop
     *            the f-stop value to set
     */
    public void setFstop(final float fstop) {
        this.fstop = fstop;
        dofShader.setUniform("fstop", fstop);
    }

    /**
     * @param gain
     *            the highlight gain to set
     */
    public void setGain(final float gain) {
        this.gain = gain;
        dofShader.setUniform("gain", gain);
    }

    /**
     * @param manualDOF
     *            whether to use manual dof calculation
     */
    public void setManualDOF(final boolean manualDOF) {
        this.manualDOF = manualDOF;
        dofShader.setUniform("manualDOF", manualDOF);
    }

    /**
     * @param maxBlur
     *            clamp value of max blur (0.0 = no blur,1.0 default)
     */
    public void setMaxBlur(final float maxBlur) {
        this.maxBlur = maxBlur;
        dofShader.setUniform("maxBlur", maxBlur);
    }

    /**
     * @param nearDOFDistance
     *            how far the depth-of-field blur reaches from the near start.
     */
    public void setNearDOFDistance(final float nearDOFDistance) {
        this.nearDOFDistance = nearDOFDistance;
        dofShader.setUniform("nearDOFDistance", nearDOFDistance);
    }

    /**
     * @param nearDOFStart
     *            where the depth-of-field blur starts towards the camera.
     */
    public void setNearDOFStart(final float nearDOFStart) {
        this.nearDOFStart = nearDOFStart;
        dofShader.setUniform("nearDOFStart", nearDOFStart);
    }

    /**
     * @param noise
     *            whether to use noise instead of pattern for sample dithering
     */
    public void setNoise(final boolean noise) {
        this.noise = noise;
        dofShader.setUniform("noise", noise);
    }

    /**
     * @param noiseDitherAmount
     *            the dither amount to set
     */
    public void setNoiseDitherAmount(final float noiseDitherAmount) {
        this.noiseDitherAmount = noiseDitherAmount;
        dofShader.setUniform("noiseDitherAmount", noiseDitherAmount);
    }

    /**
     * @param pentagonBokeh
     *            whether to use a pentagon bokeh shape
     */
    public void setPentagonBokeh(final boolean pentagonBokeh) {
        this.pentagonBokeh = pentagonBokeh;
        dofShader.setUniform("pentagonBokeh", pentagonBokeh);
    }

    /**
     * @param pentagonFeather
     *            the pentagon shape feather to set
     */
    public void setPentagonFeather(final float pentagonFeather) {
        this.pentagonFeather = pentagonFeather;
        dofShader.setUniform("pentagonFeather", pentagonFeather);
    }

    /**
     * @param rings
     *            the ring count
     */
    public void setRings(final int rings) {
        this.rings = rings;
        dofShader.setUniform("rings", rings);
    }

    /**
     * @param samples
     *            the samples on the first ring
     */
    public void setSamples(final int samples) {
        this.samples = samples;
        dofShader.setUniform("samples", samples);
    }

    /**
     * @param showFocus
     *            whether to show debug focus point and focal range (red = focal
     *            point, green = focal range)
     */
    public void setShowFocus(final boolean showFocus) {
        this.showFocus = showFocus;
        dofShader.setUniform("showFocus", showFocus);
    }

    /**
     * @param threshold
     *            the highlight threshold to set
     */
    public void setThreshold(final float threshold) {
        this.threshold = threshold;
        dofShader.setUniform("threshold", threshold);
    }

    /**
     * @param vignetting
     *            whether to use optical lens vignetting
     */
    public void setVignetting(final boolean vignetting) {
        this.vignetting = vignetting;
        dofShader.setUniform("vignetting", vignetting);
    }

    /**
     * @param vignettingFade
     *            f-stops till vignette fades
     */
    public void setVignettingFade(final float vignettingFade) {
        this.vignettingFade = vignettingFade;
        dofShader.setUniform("vignettingFade", vignettingFade);
    }

    /**
     * @param vignettingInnerBorder
     *            the vignetting inner border to set
     */
    public void setVignettingInnerBorder(final float vignettingInnerBorder) {
        this.vignettingInnerBorder = vignettingInnerBorder;
        dofShader.setUniform("vignettingInnerBorder", vignettingInnerBorder);
    }

    /**
     * @param vignettingOuterBorder
     *            the vignetting outer border to set
     */
    public void setVignettingOuterBorder(final float vignettingOuterBorder) {
        this.vignettingOuterBorder = vignettingOuterBorder;
        dofShader.setUniform("vignettingOuterBorder", vignettingOuterBorder);
    }

    /**
     * @return whether to use autoFocus in shader. Disable if you use an
     *         external focalDepth value.
     */
    public boolean usingAutoFocus() {
        return autoFocus;
    }

    /**
     * @return whether to blur the depth buffer
     */
    public boolean usingDepthBlur() {
        return depthBlur;
    }

    /**
     * @return whether to use manual dof calculation
     */
    public boolean usingManualDOF() {
        return manualDOF;
    }

    /**
     * @return whether to use noise instead of pattern for sample dithering
     */
    public boolean usingNoise() {
        return noise;
    }

    /**
     * @return whether to use a pentagon bokeh shape
     */
    public boolean usingPentagonBokeh() {
        return pentagonBokeh;
    }

    /**
     * @return whether to use optical lens vignetting
     */
    public boolean usingVignetting() {
        return vignetting;
    }

    private void ensurePassRenderer(final Renderer r) {
        if (dofRenderer == null) {
            dofRenderer =
                    TextureRendererFactory.INSTANCE.createTextureRenderer(
                            displaySettings, false, r, ContextManager
                                    .getCurrentContext().getCapabilities());
            dofRenderer
                    .setBackgroundColor(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f));
            dofRenderer.setupTexture(screenTexture);
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

            fullScreenQuad.setRenderState(dofTextureState);
            fullScreenQuad.setRenderState(dofShader);
            fullScreenQuad.updateWorldRenderStates(false);
        }
    }

    private void writeUniforms() {
        dofShader.setUniform("focalDepth", focalDepth);
        dofShader.setUniform("focalLength", focalLength);
        dofShader.setUniform("fstop", fstop);
        dofShader.setUniform("showFocus", showFocus);
        dofShader.setUniform("samples", samples);
        dofShader.setUniform("rings", rings);
        dofShader.setUniform("manualDOF", manualDOF);
        dofShader.setUniform("nearDOFStart", nearDOFStart);
        dofShader.setUniform("nearDOFDistance", nearDOFDistance);
        dofShader.setUniform("farDOFStart", farDOFStart);
        dofShader.setUniform("farDOFDistance", farDOFDistance);
        dofShader.setUniform("coc", coc);
        dofShader.setUniform("vignetting", vignetting);
        dofShader.setUniform("vignettingOuterBorder", vignettingOuterBorder);
        dofShader.setUniform("vignettingInnerBorder", vignettingInnerBorder);
        dofShader.setUniform("vignettingFade", vignettingFade);
        dofShader.setUniform("autoFocus", autoFocus);
        dofShader.setUniform("focus", focus);
        dofShader.setUniform("maxBlur", maxBlur);
        dofShader.setUniform("threshold", threshold);
        dofShader.setUniform("gain", gain);
        dofShader.setUniform("bias", bias);
        dofShader.setUniform("fringe", fringe);
        dofShader.setUniform("noise", noise);
        dofShader.setUniform("noiseDitherAmount", noiseDitherAmount);
        dofShader.setUniform("depthBlur", depthBlur);
        dofShader.setUniform("depthBlurSize", depthBlurSize);
        dofShader.setUniform("pentagonBokeh", pentagonBokeh);
        dofShader.setUniform("pentagonFeather", pentagonFeather);
    }

    @Override
    protected void doRender(final Renderer r) {
        ensurePassRenderer(r);
        final Camera cam = Camera.getCurrentCamera();

        dofRenderer.copyToTexture(screenTexture, 0, 0, cam.getWidth(),
                cam.getHeight(), 0, 0);

        r.draw((Renderable) fullScreenQuad);
    }

}
