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

public class NormalPass extends Pass {
    private static final long serialVersionUID = -4575555443278463613L;

    private final Spatial rootNode;
    private final GLSLShaderObjectsState normalShader;
    private TextureRenderer normalRenderer;
    private final Texture2D normalTexture;

    public Texture2D getNormalTexture() {
        return normalTexture;
    }

    private final TextureState emptyTexture;
    private final ClipState emptyClip;
    private final CullState cullBackFace;
    private final LightState emptyLights;

    public NormalPass(final Spatial rootNode) {
        this.rootNode = rootNode;

        normalTexture = new Texture2D();
        normalTexture.setTextureStoreFormat(TextureStoreFormat.RGB8);
        normalTexture.setWrap(Texture.WrapMode.Clamp);
        normalTexture
                .setMagnificationFilter(Texture.MagnificationFilter.Bilinear);

        normalShader = Shaders.loadShader("normal");

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
        if (normalRenderer == null) {
            final Camera cam = Camera.getCurrentCamera();
            normalRenderer =
                    TextureRendererFactory.INSTANCE.createTextureRenderer(cam
                            .getWidth(), cam.getHeight(), r, ContextManager
                            .getCurrentContext().getCapabilities());
            normalRenderer.setBackgroundColor(new ColorRGBA(0.0f, 0.0f, 0.0f,
                    0.0f));
            normalRenderer.setupTexture(normalTexture);
            normalRenderer.enforceState(emptyTexture);
            normalRenderer.enforceState(emptyClip);
            normalRenderer.enforceState(cullBackFace);
            normalRenderer.enforceState(emptyLights);
            normalRenderer.enforceState(normalShader);
        }
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (normalRenderer != null) {
            normalRenderer.cleanup();
        }
    }

    @Override
    protected void doRender(final Renderer r) {
        ensurePassRenderer(r);

        final Camera cam = Camera.getCurrentCamera();

        normalRenderer.getCamera().setFrustum(cam.getFrustumNear(),
                cam.getFrustumFar(), cam.getFrustumLeft(),
                cam.getFrustumRight(), cam.getFrustumTop(),
                cam.getFrustumBottom());
        normalRenderer.getCamera().setLocation(cam.getLocation());
        normalRenderer.getCamera().setDirection(cam.getDirection());
        normalRenderer.getCamera().setUp(cam.getUp());
        normalRenderer.getCamera().setLeft(cam.getLeft());

        normalRenderer.render(rootNode, normalTexture,
                Renderer.BUFFER_COLOR_AND_DEPTH);
    }

}
