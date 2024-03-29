package org.marble.special;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.graphical.Graphical;

public class Image extends AbstractEntity implements Graphical {
    private final float angleX, angleY, angleZ;
    private Geometry graphicalQuad;
    private final String imageFile;
    private final float width, height;

    public Image(final String imageFile, final float width, final float height) {
        this(imageFile, width, height, 0);
    }

    public Image(final String imageFile, final float width, final float height,
            final float inclination) {
        this(imageFile, width, height, inclination, 0, 0);
    }

    public Image(final String imageFile, final float width, final float height,
            final float angleX, final float angleY, final float angleZ) {
        this.imageFile = imageFile;
        this.width = width;
        this.height = height;
        this.angleX = angleX * FastMath.DEG_TO_RAD;
        this.angleY = angleY * FastMath.DEG_TO_RAD;
        this.angleZ = angleZ * FastMath.DEG_TO_RAD;
    }

    @Override
    public void initialize(final Game game) {
        final AssetManager assetManager = game.getAssetManager();
        graphicalQuad = new Geometry("image", new Quad(width, height, true));
        graphicalQuad.setLocalTranslation(-width / 2, -height / 2, 0);
        final Material mat =
                new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        final Texture tex = assetManager.loadTexture(imageFile);
        mat.setTexture("DiffuseMap", tex);
        mat.setBoolean("UseAlpha", true);
        mat.setFloat("AlphaDiscardThreshold", 0.05f);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        graphicalQuad.setMaterial(mat);

        final Node helper = new Node("helper");
        helper.attachChild(graphicalQuad);
        helper.setLocalRotation(new Quaternion().fromAngles(angleX, angleY,
                angleZ));
        getSpatial().attachChild(helper);
    }
}
