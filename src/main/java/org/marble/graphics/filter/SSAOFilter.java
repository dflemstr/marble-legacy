package org.marble.graphics.filter;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;

import com.google.common.collect.ImmutableList;

public class SSAOFilter extends Filter {
    private final float downsamples = 1f;
    private float sampleRadius = 1.0f;
    private float intensity = 8.0f;
    private float scale = 1.0f;
    private float bias = 0.1f;
    private float cutoff = 0.99f;
    private boolean showOnlyAO = false;
    private boolean disableBlur = false;
    private Filter.Pass normalPass;
    private Material ssaoMaterial;
    private Pass ssaoPass;

    private final Vector3f frustumCorner = new Vector3f();

    public SSAOFilter() {
        super("SSAOFilter");
    }

    @Override
    protected boolean isRequiresDepthTexture() {
        return true;
    }

    @Override
    protected void postQueue(final RenderManager renderManager,
            final ViewPort viewPort) {
        final Renderer r = renderManager.getRenderer();
        r.setFrameBuffer(normalPass.getRenderFrameBuffer());
        renderManager.getRenderer().clearBuffers(true, true, true);
        renderManager.setForcedTechnique("PreNormalPass");
        renderManager.renderViewPortQueues(viewPort, false);
        renderManager.setForcedTechnique(null);
        renderManager.getRenderer().setFrameBuffer(
                viewPort.getOutputFrameBuffer());
    }

    /**
     * Create a Screen Space Ambient Occlusion Filter
     * 
     * @param sampleRadius
     *            The radius of the area where random samples will be picked.
     *            default 5.1f
     * @param intensity
     *            intensity of the resulting AO. default 1.2f
     * @param scale
     *            distance between occluders and occludee. default 0.2f
     * @param bias
     *            the width of the occlusion cone considered by the occludee.
     *            default 0.1f
     */
    public SSAOFilter(final float sampleRadius, final float intensity,
            final float scale, final float bias) {
        this();
        this.sampleRadius = sampleRadius;
        this.intensity = intensity;
        this.scale = scale;
        this.bias = bias;
    }

    private void writeParams() {
        ssaoMaterial.setFloat("SampleRadius", sampleRadius);
        ssaoMaterial.setFloat("Intensity", intensity);
        ssaoMaterial.setFloat("Scale", scale);
        ssaoMaterial.setFloat("Bias", bias);
        ssaoMaterial.setFloat("Cutoff", cutoff);

        material.setBoolean("ShowOnlyAO", showOnlyAO);
        material.setBoolean("DisableBlur", disableBlur);
    }

    @Override
    protected void initFilter(final AssetManager manager,
            final RenderManager renderManager, final ViewPort vp, final int w,
            final int h) {
        final ImmutableList.Builder<Pass> postRenderPassesBuilder =
                ImmutableList.builder();

        normalPass = new Pass();
        normalPass.init(renderManager.getRenderer(), (int) (w / downsamples),
                (int) (h / downsamples), Format.RGBA8, Format.Depth);

        ssaoMaterial = new Material(manager, "MatDefs/SSAO/SSAO.j3md");
        ssaoMaterial.setTexture("NormalTexture",
                normalPass.getRenderedTexture());
        ssaoPass = new Pass() {
            @Override
            public boolean requiresDepthAsTexture() {
                return true;
            }
        };
        ssaoPass.init(renderManager.getRenderer(), (int) (w / downsamples),
                (int) (h / downsamples), Format.RGBA8, Format.Depth, 1,
                ssaoMaterial);
        ssaoPass.getRenderedTexture().setMinFilter(Texture.MinFilter.Trilinear);
        ssaoPass.getRenderedTexture().setMagFilter(Texture.MagFilter.Bilinear);
        postRenderPassesBuilder.add(ssaoPass);

        material = new Material(manager, "MatDefs/SSAO/SSAOBlur.j3md");
        material.setTexture("SSAOTexture", ssaoPass.getRenderedTexture());

        postRenderPasses = postRenderPassesBuilder.build();

        final Camera cam = vp.getCamera();

        final float farY =
                cam.getFrustumTop() / cam.getFrustumNear()
                        * cam.getFrustumFar();
        final float farX =
                farY * ((float) cam.getWidth() / (float) cam.getHeight());
        frustumCorner.set(farX, farY, cam.getFrustumFar());

        ssaoMaterial.setVector3("FrustumCorner", frustumCorner);
        ssaoMaterial.setFloat("ZNear", cam.getFrustumNear());
        ssaoMaterial.setFloat("ZFar", cam.getFrustumFar());

        material.setFloat("ZNear", cam.getFrustumNear());
        material.setFloat("ZFar", cam.getFrustumFar());

        writeParams();
    }

    @Override
    protected Material getMaterial() {
        return material;
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
        if (ssaoMaterial != null) {
            ssaoMaterial.setFloat("bias", bias);
        }
    }

    /**
     * @param cutoff
     *            the cutoff to set
     */
    public void setCutoff(final float cutoff) {
        this.cutoff = cutoff;
        if (ssaoMaterial != null) {
            ssaoMaterial.setFloat("Cutoff", cutoff);
        }
    }

    /**
     * @param disableBlur
     *            whether blur should be disabled
     */
    public void setDisableBlur(final boolean disableBlur) {
        this.disableBlur = disableBlur;
        if (material != null) {
            material.setBoolean("DisableBlur", disableBlur);
        }
    }

    /**
     * @param intensity
     *            the intensity to set
     */
    public void setIntensity(final float intensity) {
        this.intensity = intensity;
        if (ssaoMaterial != null) {
            ssaoMaterial.setFloat("Intensity", intensity);
        }
    }

    /**
     * @param sampleRadius
     *            the sample radius to set
     */
    public void setSampleRadius(final float sampleRadius) {
        this.sampleRadius = sampleRadius;
        if (ssaoMaterial != null) {
            ssaoMaterial.setFloat("SampleRadius", sampleRadius);
        }
    }

    /**
     * @param scale
     *            the scale to set
     */
    public void setScale(final float scale) {
        this.scale = scale;
        if (ssaoMaterial != null) {
            ssaoMaterial.setFloat("Scale", scale);
        }
    }

    /**
     * @param showOnlyAO
     *            whether to only render the ambient occlusion without tinting
     */
    public void setShowOnlyAO(final boolean showOnlyAO) {
        this.showOnlyAO = showOnlyAO;
        if (material != null) {
            material.setBoolean("ShowOnlyAO", showOnlyAO);
        }
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
}
