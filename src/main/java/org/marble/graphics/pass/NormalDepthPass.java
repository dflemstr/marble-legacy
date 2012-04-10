package org.marble.graphics.pass;

import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRenderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.pass.Pass;
import com.ardor3d.renderer.state.ClipState;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial;

import org.marble.util.Shaders;

public class NormalDepthPass extends Pass {
    private static final long serialVersionUID = 6442769845341890013L;

    private final Spatial rootNode;
    private final GLSLShaderObjectsState normalDepthShader;
    private TextureRenderer normalDepthRenderer;
    private final Texture2D normalDepthTexture;

    public Texture2D getNormalDepthTexture() {
        return normalDepthTexture;
    }

    private final TextureState emptyTexture;
    private final ClipState emptyClip;
    private final CullState cullBackFace;
    private final LightState emptyLights;

    public NormalDepthPass(final Spatial rootNode) {
        this.rootNode = rootNode;

        normalDepthTexture = new Texture2D();
        normalDepthTexture.setTextureStoreFormat(TextureStoreFormat.RGBA16);
        normalDepthTexture.setWrap(Texture.WrapMode.Clamp);
        normalDepthTexture
                .setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        normalDepthShader = Shaders.loadShader("normal-depth");

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

    private void ensurePassRenderer(final Renderer r) {
        if (normalDepthRenderer == null) {
            final Camera cam = Camera.getCurrentCamera();
            normalDepthRenderer =
                    TextureRendererFactory.INSTANCE.createTextureRenderer(cam
                            .getWidth(), cam.getHeight(), r, ContextManager
                            .getCurrentContext().getCapabilities());
            normalDepthRenderer.setBackgroundColor(new ColorRGBA(0.0f, 0.0f,
                    0.0f, 0.0f));
            normalDepthRenderer.getCamera().setFrustum(cam.getFrustumNear(),
                    cam.getFrustumFar(), cam.getFrustumLeft(),
                    cam.getFrustumRight(), cam.getFrustumTop(),
                    cam.getFrustumBottom());
            normalDepthRenderer.setupTexture(normalDepthTexture);
            normalDepthRenderer.enforceState(emptyTexture);
            normalDepthRenderer.enforceState(emptyClip);
            normalDepthRenderer.enforceState(cullBackFace);
            normalDepthRenderer.enforceState(emptyLights);
            normalDepthRenderer.enforceState(normalDepthShader);
        }
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (normalDepthRenderer != null) {
            normalDepthRenderer.cleanup();
        }
    }

    @Override
    protected void doRender(final Renderer r) {
        ensurePassRenderer(r);

        final Camera cam = Camera.getCurrentCamera();

        normalDepthRenderer.getCamera().setLocation(cam.getLocation());
        normalDepthRenderer.getCamera().setDirection(cam.getDirection());
        normalDepthRenderer.getCamera().setUp(cam.getUp());
        normalDepthRenderer.getCamera().setLeft(cam.getLeft());

        normalDepthRenderer.render(rootNode, normalDepthTexture,
                Renderer.BUFFER_COLOR_AND_DEPTH);
    }

}
