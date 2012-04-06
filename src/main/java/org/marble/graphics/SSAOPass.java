package org.marble.graphics;

import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.WrapMode;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextCapabilities;
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
    private final double sampleRadius;
    private final double intensity;
    private final double scale;
    private final double bias;

    private TextureRenderer passRenderer;
    private Quad fullScreenQuad;

    private final Texture2D normalTexture;
    private final Texture randomTexture;
    private final Texture2D ssaoTexture;
    private final TextureState ts;
    private final BlendState bs;
    private final GLSLShaderObjectsState ssaoShader;
    private final GLSLShaderObjectsState normalShader;

    private final Vector3 frustumCorner = new Vector3();
    private final Vector2[] samples = { new Vector2(1.0, 0.0),
            new Vector2(-1.0, 0.0), new Vector2(0.0, 1.0),
            new Vector2(0.0, -1.0) };
    private final Vector2 resolution = new Vector2();

    private final TextureState emptyTexture;
    private final ClipState emptyClip;
    private final CullState cullBackFace;
    private final LightState emptyLights;

    public SSAOPass(final Spatial rootNode, final double sampleRadius,
            final double intensity, final double scale, final double bias) {
        this.rootNode = rootNode;
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

        ts = new TextureState();
        ts.setTexture(normalTexture, 0);
        ts.setTexture(randomTexture, 1);
        ts.setEnabled(true);

        emptyClip = new ClipState();
        emptyClip.setEnabled(false);
        emptyTexture = new TextureState();
        emptyTexture.setEnabled(false);
        cullBackFace = new CullState();
        cullBackFace.setEnabled(true);
        cullBackFace.setCullFace(CullState.Face.Back);
        emptyLights = new LightState();
        emptyLights.setEnabled(false);

        bs = new BlendState();
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.Zero);
        bs.setDestinationFunction(BlendState.DestinationFunction.SourceColor);
        bs.setEnabled(true);

        ssaoShader = Shaders.loadShader("ssao");
        ssaoShader.setUniform("normals", 0);
        ssaoShader.setUniform("randoms", 1);

        normalShader = Shaders.loadShader("normal");
    }

    private TextureRenderer createTextureRenderer(final Renderer r) {
        final Camera cam = Camera.getCurrentCamera();
        final ContextCapabilities caps =
                ContextManager.getCurrentContext().getCapabilities();
        final TextureRenderer renderer =
                TextureRendererFactory.INSTANCE.createTextureRenderer(
                        cam.getWidth() / 4, cam.getHeight() / 4, r, caps);
        renderer.setMultipleTargets(true);
        renderer.setBackgroundColor(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f));
        renderer.getCamera().setFrustum(cam.getFrustumNear(),
                cam.getFrustumFar(), cam.getFrustumLeft(),
                cam.getFrustumRight(), cam.getFrustumTop(),
                cam.getFrustumBottom());
        return renderer;
    }

    private void ensurePassRenderer(final Renderer r) {
        if (passRenderer == null) {
            passRenderer = createTextureRenderer(r);
            passRenderer.setupTexture(normalTexture);

            final Camera cam = Camera.getCurrentCamera();
            fullScreenQuad =
                    new Quad("FullScreenQuad", cam.getWidth() / 4,
                            cam.getHeight() / 4);
            fullScreenQuad.setTranslation(cam.getWidth() / 2,
                    cam.getHeight() / 2, 0);
            fullScreenQuad.getSceneHints().setRenderBucketType(
                    RenderBucketType.Ortho);

            fullScreenQuad.getSceneHints().setCullHint(CullHint.Never);
            fullScreenQuad.setRenderState(ts);
            fullScreenQuad.setRenderState(bs);
            fullScreenQuad.setRenderState(ssaoShader);

            fullScreenQuad.updateGeometricState(0.0f, true);
        }
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (passRenderer != null) {
            passRenderer.cleanup();
        }
    }

    @Override
    protected void doRender(final Renderer r) {
        ensurePassRenderer(r);
        final Camera cam = Camera.getCurrentCamera();

        passRenderer.getCamera().setLocation(cam.getLocation());
        passRenderer.getCamera().setDirection(cam.getDirection());
        passRenderer.getCamera().setUp(cam.getUp());
        passRenderer.getCamera().setLeft(cam.getLeft());

        final double farY =
                (cam.getFrustumTop() / cam.getFrustumNear())
                        * cam.getFrustumFar();
        final double farX =
                farY * ((double) cam.getWidth() / (double) cam.getHeight());
        frustumCorner.set(farX, farY, cam.getFrustumFar());

        resolution.set(cam.getWidth(), cam.getHeight());

        passRenderer.enforceState(emptyTexture);
        passRenderer.enforceState(emptyClip);
        passRenderer.enforceState(cullBackFace);
        passRenderer.enforceState(emptyLights);
        passRenderer.enforceState(normalShader);
        passRenderer.render(rootNode, normalTexture,
                Renderer.BUFFER_COLOR_AND_DEPTH);
        passRenderer.clearEnforcedStates();

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

        r.draw((Renderable) fullScreenQuad);
    }
}
