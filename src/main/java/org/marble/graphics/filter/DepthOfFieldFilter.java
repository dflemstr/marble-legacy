package org.marble.graphics.filter;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.post.Filter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;

public class DepthOfFieldFilter extends Filter {
    private boolean autoFocus = true;
    private float bias = 0.2f;
    private float coc = 0.03f;
    private boolean depthBlur = true;
    private float depthBlurSize = 1.25f;
    private float farDOFDistance = 3.0f;
    private float farDOFStart = 1.0f;
    private float focalDepth = 0.0f;
    private float focalLength = 40.0f;
    private final Vector2f focus = new Vector2f(0.5f, 0.5f);
    private float fringe = 0.7f;
    private float fstop = 1.4f;
    private float gain = 25.0f;
    private boolean manualDOF = false;
    private float maxBlur = 1.0f;
    private float nearDOFDistance = 2.0f;
    private float nearDOFStart = 1.0f;
    private boolean noise = true;
    private float noiseDitherAmount = 0.0001f;
    private boolean pentagonBokeh = false;
    private float pentagonFeather = 0.4f;
    private int rings = 3;
    private int samples = 3;
    private boolean showFocus = false;
    private float threshold = 0.5f;
    private boolean vignetting = false;
    private float vignettingFade = 22.0f;
    private float vignettingInnerBorder = 0.0f;
    private float vignettingOuterBorder = 1.3f;

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
    public Vector2f getFocus() {
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
        if (material != null) {
            material.setBoolean("AutoFocus", autoFocus);
        }
    }

    /**
     * @param bias
     *            the bokeh edge bias to set
     */
    public void setBias(final float bias) {
        this.bias = bias;
        if (material != null) {
            material.setFloat("Bias", bias);
        }
    }

    /**
     * @param coc
     *            the circle of confusion size in mm (35mm film = 0.03mm)
     */
    public void setCoC(final float coc) {
        this.coc = coc;
        if (material != null) {
            material.setFloat("CoC", coc);
        }
    }

    /**
     * @param depthBlur
     *            whether to blur the depth buffer
     */
    public void setDepthBlur(final boolean depthBlur) {
        this.depthBlur = depthBlur;
        if (material != null) {
            material.setBoolean("DepthBlur", depthBlur);
        }
    }

    /**
     * @param depthBlurSize
     *            the depth blur size to set
     */
    public void setDepthBlurSize(final float depthBlurSize) {
        this.depthBlurSize = depthBlurSize;
        if (material != null) {
            material.setFloat("DepthBlurSize", depthBlurSize);
        }
    }

    /**
     * @param farDOFDistance
     *            the how far the depth-of-field blur reaches from the far
     *            start.
     */
    public void setFarDOFDistance(final float farDOFDistance) {
        this.farDOFDistance = farDOFDistance;
        if (material != null) {
            material.setFloat("FarDOFDistance", farDOFDistance);
        }
    }

    /**
     * @param farDOFStart
     *            where the depth-of-field blur starts away from the camera.
     */
    public void setFarDOFStart(final float farDOFStart) {
        this.farDOFStart = farDOFStart;
        if (material != null) {
            material.setFloat("FarDOFStart", farDOFStart);
        }
    }

    /**
     * @param focalDepth
     *            the the focal distance value in meters to set
     */
    public void setFocalDepth(final float focalDepth) {
        this.focalDepth = focalDepth;
        if (material != null) {
            material.setFloat("FocalDepth", focalDepth);
        }
    }

    /**
     * @param focalLength
     *            the focal length in mm to set
     */
    public void setFocalLength(final float focalLength) {
        this.focalLength = focalLength;
        if (material != null) {
            material.setFloat("FocalLength", focalLength);
        }
    }

    /**
     * @param focus
     *            auto focus point on screen (0.0,0.0 - left lower corner,
     *            1.0,1.0 - upper right)
     */
    public void setFocus(final Vector2f focus) {
        this.focus.set(focus);
        if (material != null) {
            material.setVector2("Focus", focus);
        }
    }

    /**
     * @param fringe
     *            the bokeh chromatic aberration/fringing to set
     */
    public void setFringe(final float fringe) {
        this.fringe = fringe;
        if (material != null) {
            material.setFloat("Fringe", fringe);
        }
    }

    /**
     * @param fstop
     *            the f-stop value to set
     */
    public void setFstop(final float fstop) {
        this.fstop = fstop;
        if (material != null) {
            material.setFloat("FStop", fstop);
        }
    }

    /**
     * @param gain
     *            the highlight gain to set
     */
    public void setGain(final float gain) {
        this.gain = gain;
        if (material != null) {
            material.setFloat("Gain", gain);
        }
    }

    /**
     * @param manualDOF
     *            whether to use manual dof calculation
     */
    public void setManualDOF(final boolean manualDOF) {
        this.manualDOF = manualDOF;
        if (material != null) {
            material.setBoolean("ManualDOF", manualDOF);
        }
    }

    /**
     * @param maxBlur
     *            clamp value of max blur (0.0 = no blur,1.0 default)
     */
    public void setMaxBlur(final float maxBlur) {
        this.maxBlur = maxBlur;
        if (material != null) {
            material.setFloat("MaxBlur", maxBlur);
        }
    }

    /**
     * @param nearDOFDistance
     *            how far the depth-of-field blur reaches from the near start.
     */
    public void setNearDOFDistance(final float nearDOFDistance) {
        this.nearDOFDistance = nearDOFDistance;
        if (material != null) {
            material.setFloat("NearDOFDistance", nearDOFDistance);
        }
    }

    /**
     * @param nearDOFStart
     *            where the depth-of-field blur starts towards the camera.
     */
    public void setNearDOFStart(final float nearDOFStart) {
        this.nearDOFStart = nearDOFStart;
        if (material != null) {
            material.setFloat("NearDOFStart", nearDOFStart);
        }
    }

    /**
     * @param noise
     *            whether to use noise instead of pattern for sample dithering
     */
    public void setNoise(final boolean noise) {
        this.noise = noise;
        if (material != null) {
            material.setBoolean("Noise", noise);
        }
    }

    /**
     * @param noiseDitherAmount
     *            the dither amount to set
     */
    public void setNoiseDitherAmount(final float noiseDitherAmount) {
        this.noiseDitherAmount = noiseDitherAmount;
        if (material != null) {
            material.setFloat("NoiseDitherAmount", noiseDitherAmount);
        }
    }

    /**
     * @param pentagonBokeh
     *            whether to use a pentagon bokeh shape
     */
    public void setPentagonBokeh(final boolean pentagonBokeh) {
        this.pentagonBokeh = pentagonBokeh;
        if (material != null) {
            material.setBoolean("PentagonBokeh", pentagonBokeh);
        }
    }

    /**
     * @param pentagonFeather
     *            the pentagon shape feather to set
     */
    public void setPentagonFeather(final float pentagonFeather) {
        this.pentagonFeather = pentagonFeather;
        if (material != null) {
            material.setFloat("PentagonFeather", pentagonFeather);
        }
    }

    /**
     * @param rings
     *            the ring count
     */
    public void setRings(final int rings) {
        this.rings = rings;
        if (material != null) {
            material.setInt("Rings", rings);
        }
    }

    /**
     * @param samples
     *            the samples on the first ring
     */
    public void setSamples(final int samples) {
        this.samples = samples;
        if (material != null) {
            material.setInt("Samples", samples);
        }
    }

    /**
     * @param showFocus
     *            whether to show debug focus point and focal range (red = focal
     *            point, green = focal range)
     */
    public void setShowFocus(final boolean showFocus) {
        this.showFocus = showFocus;
        if (material != null) {
            material.setBoolean("ShowFocus", showFocus);
        }
    }

    /**
     * @param threshold
     *            the highlight threshold to set
     */
    public void setThreshold(final float threshold) {
        this.threshold = threshold;
        if (material != null) {
            material.setFloat("Threshold", threshold);
        }
    }

    /**
     * @param vignetting
     *            whether to use optical lens vignetting
     */
    public void setVignetting(final boolean vignetting) {
        this.vignetting = vignetting;
        if (material != null) {
            material.setBoolean("Vignetting", vignetting);
        }
    }

    /**
     * @param vignettingFade
     *            f-stops till vignette fades
     */
    public void setVignettingFade(final float vignettingFade) {
        this.vignettingFade = vignettingFade;
        if (material != null) {
            material.setFloat("VignettingFade", vignettingFade);
        }
    }

    /**
     * @param vignettingInnerBorder
     *            the vignetting inner border to set
     */
    public void setVignettingInnerBorder(final float vignettingInnerBorder) {
        this.vignettingInnerBorder = vignettingInnerBorder;
        if (material != null) {
            material.setFloat("VignettingInnerBorder", vignettingInnerBorder);
        }
    }

    /**
     * @param vignettingOuterBorder
     *            the vignetting outer border to set
     */
    public void setVignettingOuterBorder(final float vignettingOuterBorder) {
        this.vignettingOuterBorder = vignettingOuterBorder;
        if (material != null) {
            material.setFloat("VignettingOuterBorder", vignettingOuterBorder);
        }
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

    private void writeParams() {
        material.setFloat("FocalDepth", focalDepth);
        material.setFloat("FocalLength", focalLength);
        material.setFloat("FStop", fstop);
        material.setBoolean("ShowFocus", showFocus);
        material.setInt("Samples", samples);
        material.setInt("Rings", rings);
        material.setBoolean("ManualDOF", manualDOF);
        material.setFloat("NearDOFStart", nearDOFStart);
        material.setFloat("NearDOFDistance", nearDOFDistance);
        material.setFloat("FarDOFStart", farDOFStart);
        material.setFloat("FarDOFDistance", farDOFDistance);
        material.setFloat("CoC", coc);
        material.setBoolean("Vignetting", vignetting);
        material.setFloat("VignettingOuterBorder", vignettingOuterBorder);
        material.setFloat("VignettingInnerBorder", vignettingInnerBorder);
        material.setFloat("VignettingFade", vignettingFade);
        material.setBoolean("AutoFocus", autoFocus);
        material.setVector2("Focus", focus);
        material.setFloat("MaxBlur", maxBlur);
        material.setFloat("Threshold", threshold);
        material.setFloat("Gain", gain);
        material.setFloat("Bias", bias);
        material.setFloat("Fringe", fringe);
        material.setBoolean("Noise", noise);
        material.setFloat("NoiseDitherAmount", noiseDitherAmount);
        material.setBoolean("DepthBlur", depthBlur);
        material.setFloat("DepthBlurSize", depthBlurSize);
        material.setBoolean("PentagonBokeh", pentagonBokeh);
        material.setFloat("PentagonFeather", pentagonFeather);
    }

    @Override
    protected Material getMaterial() {
        return material;
    }

    @Override
    protected void initFilter(final AssetManager manager,
            final RenderManager renderManager, final ViewPort vp, final int w,
            final int h) {
        defaultPass = new Pass();
        defaultPass.init(renderManager.getRenderer(), w, h,
                getDefaultPassTextureFormat(), getDefaultPassDepthFormat());

        material =
                new Material(manager, "MatDefs/DepthOfField/DepthOfField.j3md");
        final Camera cam = vp.getCamera();
        material.setFloat("ZNear", cam.getFrustumNear());
        material.setFloat("ZFar", cam.getFrustumFar());
        writeParams();
    }

    @Override
    protected boolean isRequiresDepthTexture() {
        return true;
    }
}
