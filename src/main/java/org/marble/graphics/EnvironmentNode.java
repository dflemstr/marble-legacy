package org.marble.graphics;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.TextureCubeMap;

import org.marble.frp.FRPUtils;
import org.marble.frp.Reactive;
import org.marble.frp.ReactiveListener;
import org.marble.frp.ReactiveReference;

/**
 * A node that acts like a panorama camera, rendering to a cube map instead of a
 * texture.
 */
public class EnvironmentNode extends Node {
    private static final Vector3f[][] cameraAngles = {
            { Vector3f.UNIT_Z, Vector3f.UNIT_Y.negate(), Vector3f.UNIT_X },
            { Vector3f.UNIT_Z.negate(), Vector3f.UNIT_Y.negate(),
                    Vector3f.UNIT_X.negate() },
            { Vector3f.UNIT_X.negate(), Vector3f.UNIT_Z, Vector3f.UNIT_Y },
            { Vector3f.UNIT_X.negate(), Vector3f.UNIT_Z.negate(),
                    Vector3f.UNIT_Y.negate() },
            { Vector3f.UNIT_X.negate(), Vector3f.UNIT_Y.negate(),
                    Vector3f.UNIT_Z },
            { Vector3f.UNIT_X, Vector3f.UNIT_Y.negate(),
                    Vector3f.UNIT_Z.negate() } };

    private final ReactiveReference<TextureCubeMap> environment;
    private final FrameBuffer[] environmentBuffers;
    private final Camera[] environmentCameras;

    private final ViewPort[] environmentViews;

    private final TextureSizeListener listener = new TextureSizeListener();

    private final RenderManager renderManager;
    private final Spatial root;
    private final Reactive<Integer> textureSizeMagnitude;

    /**
     * Creates a new environment node.
     */
    public EnvironmentNode(final Spatial root,
            final RenderManager renderManager,
            final Reactive<Integer> textureSizeMagnitude) {
        this.root = root;
        this.renderManager = renderManager;
        this.textureSizeMagnitude = textureSizeMagnitude;

        environmentBuffers = new FrameBuffer[6];
        environmentCameras = new Camera[6];
        environmentViews = new ViewPort[6];

        environment = new ReactiveReference<TextureCubeMap>(null);

        FRPUtils.addAndCallReactiveListener(textureSizeMagnitude, listener);
    }

    public void destroy() {
        if (renderManager != null) {
            for (final ViewPort view : environmentViews) {
                renderManager.removePreView(view);
            }
        }
        textureSizeMagnitude.removeReactiveListener(listener);
    }

    public Reactive<TextureCubeMap> getEnvironment() {
        return environment;
    }

    @Override
    public void updateGeometricState() {
        for (int i = 0; i < 6; i++) {
            environmentCameras[i].setLocation(getWorldTranslation());
        }
        super.updateGeometricState();
    }

    private void updateRenderer(final int textureSizeMagnitude) {
        final int textureSize = 1 << textureSizeMagnitude;

        final TextureCubeMap env =
                new TextureCubeMap(textureSize, textureSize, Format.RGB8);
        env.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        env.setMagFilter(Texture.MagFilter.Bilinear);
        environment.setValue(env);

        for (int i = 0; i < 6; i++) {
            environmentCameras[i] = new Camera(textureSize, textureSize);
            environmentCameras[i].setFrustum(0.0625f, 1024.0f, -0.0625f,
                    0.0625f, 0.0625f, -0.0625f);
            environmentCameras[i].setLocation(getWorldTranslation());
            environmentCameras[i].setAxes(cameraAngles[i][0],
                    cameraAngles[i][1], cameraAngles[i][2]);

            environmentBuffers[i] =
                    new FrameBuffer(textureSize, textureSize, 1);
            environmentBuffers[i].setColorTexture(env,
                    TextureCubeMap.Face.values()[i]);
            environmentBuffers[i].setDepthBuffer(Format.Depth);

            environmentCameras[i].resize(textureSize, textureSize, false);

            if (environmentViews[i] == null) {
                environmentViews[i] =
                        renderManager.createPreView(getName() + " environment "
                                + (i + 1), environmentCameras[i]);
            }
            environmentViews[i].setClearFlags(true, true, true);
            environmentViews[i].setBackgroundColor(ColorRGBA.Black);
            environmentViews[i].setOutputFrameBuffer(environmentBuffers[i]);
            environmentViews[i].attachScene(root);
        }
    }

    @Override
    protected void finalize() {
        destroy();
    }

    private final class TextureSizeListener implements
            ReactiveListener<Integer> {
        @Override
        public void valueChanged(final Integer value) {
            updateRenderer(value);
        }
    }
}
