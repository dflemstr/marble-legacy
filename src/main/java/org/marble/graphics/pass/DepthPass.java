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

public class DepthPass extends Pass {
    private static final long serialVersionUID = 6442769845341890013L;

    private final Spatial rootNode;
    private final GLSLShaderObjectsState emptyShader;
    private TextureRenderer depthRenderer;
    private final Texture2D depthTexture;

    public Texture2D getDepthTexture() {
        return depthTexture;
    }

    private final TextureState emptyTexture;
    private final ClipState emptyClip;
    private final CullState cullBackFace;
    private final LightState emptyLights;

    public DepthPass(final Spatial rootNode) {
        this.rootNode = rootNode;

        depthTexture = new Texture2D();
        depthTexture.setTextureStoreFormat(TextureStoreFormat.Depth32);
        depthTexture.setWrap(Texture.WrapMode.Clamp);
        depthTexture
                .setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
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

    private void ensurePassRenderer(final Renderer r) {
        if (depthRenderer == null) {
            final Camera cam = Camera.getCurrentCamera();
            depthRenderer =
                    TextureRendererFactory.INSTANCE.createTextureRenderer(cam
                            .getWidth(), cam.getHeight(), r, ContextManager
                            .getCurrentContext().getCapabilities());
            depthRenderer.setBackgroundColor(new ColorRGBA(0.0f, 0.0f, 0.0f,
                    0.0f));
            depthRenderer.setupTexture(depthTexture);
            depthRenderer.enforceState(emptyTexture);
            depthRenderer.enforceState(emptyClip);
            depthRenderer.enforceState(cullBackFace);
            depthRenderer.enforceState(emptyLights);
            depthRenderer.enforceState(emptyShader);
        }
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (depthRenderer != null) {
            depthRenderer.cleanup();
        }
    }

    @Override
    protected void doRender(final Renderer r) {
        ensurePassRenderer(r);

        final Camera cam = Camera.getCurrentCamera();

        depthRenderer.getCamera().setFrustum(cam.getFrustumNear(),
                cam.getFrustumFar(), cam.getFrustumLeft(),
                cam.getFrustumRight(), cam.getFrustumTop(),
                cam.getFrustumBottom());
        depthRenderer.getCamera().setLocation(cam.getLocation());
        depthRenderer.getCamera().setDirection(cam.getDirection());
        depthRenderer.getCamera().setUp(cam.getUp());
        depthRenderer.getCamera().setLeft(cam.getLeft());

        depthRenderer.render(rootNode, depthTexture,
                Renderer.BUFFER_COLOR_AND_DEPTH);
    }

}
