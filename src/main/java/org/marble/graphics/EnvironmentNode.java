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

/**
 * A node that acts like a panorama camera, rendering to a cube map instead of a
 * texture.
 */
public class EnvironmentNode extends Node {
    private final Spatial root;
    private final RenderManager renderManager;
    private int textureSizeMagnitude;

    private final TextureCubeMap environment;

    /**
     * @return the environment
     */
    public TextureCubeMap getEnvironment() {
        return environment;
    }

    private final FrameBuffer[] environmentBuffers;
    private final ViewPort[] environmentViews;
    private final Camera[] environmentCameras;

    private final Vector3f[][] cameraAngles = {
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

    /**
     * Creates a new environment node.
     */
    public EnvironmentNode(final Spatial root,
            final RenderManager renderManager, final int textureSizeMagnitude) {
        this.root = root;
        this.renderManager = renderManager;
        this.textureSizeMagnitude = textureSizeMagnitude;

        final int textureSize = 1 << textureSizeMagnitude;

        environment = new TextureCubeMap(textureSize, textureSize, Format.RGB8);
        environment.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        environment.setMagFilter(Texture.MagFilter.Bilinear);

        environmentBuffers = new FrameBuffer[6];
        environmentCameras = new Camera[6];
        environmentViews = new ViewPort[6];

        for (int i = 0; i < 6; i++) {
            environmentCameras[i] = new Camera(textureSize, textureSize);
            environmentCameras[i].setFrustum(0.0625f, 1024.0f, -0.0625f,
                    0.0625f, 0.0625f, -0.0625f);
            environmentCameras[i].setLocation(getWorldTranslation());
            environmentCameras[i].setAxes(cameraAngles[i][0],
                    cameraAngles[i][1], cameraAngles[i][2]);
        }

        updateRenderer(textureSizeMagnitude);
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

        for (int i = 0; i < 6; i++) {
            environmentBuffers[i] =
                    new FrameBuffer(textureSize, textureSize, 1);
            environmentBuffers[i].setColorTexture(environment,
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

    public int getTextureSizeMagnitude() {
        return textureSizeMagnitude;
    }

    public void setTextureSizeMagnitude(final int textureSizeMagnitude) {
        if (textureSizeMagnitude != this.textureSizeMagnitude) {
            updateRenderer(textureSizeMagnitude);
        }
        this.textureSizeMagnitude = textureSizeMagnitude;
    }

    @Override
    protected void finalize() {
        if (renderManager != null) {
            for (final ViewPort view : environmentViews) {
                renderManager.removePreView(view);
            }
        }
    }
}
