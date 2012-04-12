package org.marble.graphics.pass;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
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

public class DepthOfFieldPass extends Pass {
    private static final long serialVersionUID = 161203858061648402L;

    private TextureRenderer dofRenderer;
    private Quad fullScreenQuad;

    private final Texture2D screenTexture;

    private final TextureState dofTextureState;
    private final GLSLShaderObjectsState dofShader;

    private final Vector2 resolution = new Vector2();

    private void ensurePassRenderer(final Renderer r) {
        if (dofRenderer == null) {
            final Camera cam = Camera.getCurrentCamera();
            final DisplaySettings settings =
                    new DisplaySettings(cam.getWidth(), cam.getHeight(), 24, 0,
                            0, 8, 0, 0, false, false);

            dofRenderer =
                    TextureRendererFactory.INSTANCE.createTextureRenderer(
                            settings, false, r, ContextManager
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

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (dofRenderer != null) {
            dofRenderer.cleanup();
        }
    }

    public DepthOfFieldPass(final Texture2D depthTexture,
            final double focalLength, final double fstop) {
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
    }

    @Override
    protected void doRender(final Renderer r) {
        ensurePassRenderer(r);
        final Camera cam = Camera.getCurrentCamera();

        resolution.set(cam.getWidth(), cam.getHeight());

        dofRenderer.copyToTexture(screenTexture, 0, 0, cam.getWidth(),
                cam.getHeight(), 0, 0);

        dofShader.setUniform("resolution", resolution);
        dofShader.setUniform("znear", (float) cam.getFrustumNear());
        dofShader.setUniform("zfar", (float) cam.getFrustumFar());

        r.draw((Renderable) fullScreenQuad);
    }

}
