package org.marble.graphics;

import java.util.ArrayList;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.shader.Shader;
import com.jme3.shader.Uniform;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture;

public class MaterialSP extends Material {
    protected ArrayList<Light> lightList = new ArrayList<Light>(4);

    public MaterialSP(final MaterialDef def) {
        super(def);
    }

    public MaterialSP(final AssetManager assetManager, final String defName) {
        super(assetManager, defName);
    }

    public MaterialSP(final Material mat) {
        super(mat.getMaterialDef());

        for (final MatParam param : mat.getParams()) {
            if (param instanceof MatParamTexture) {
                setTextureParam(param.getName(), param.getVarType(),
                        (Texture) param.getValue());
            } else {
                setParam(param.getName(), param.getVarType(), param.getValue());
            }
        }
    }

    public MaterialSP(final String matName, final AssetManager assetManager) {
        this(assetManager.loadMaterial(matName));
    }

    /**
     * Do not use this constructor. Serialization purposes only.
     */
    public MaterialSP() {
    }

    @Override
    protected void updateLightListUniforms(final Shader shader,
            final Geometry g, int numLights) {
        if (numLights == 0)
            return;

        final LightList worldLightList = g.getWorldLightList();
        final ColorRGBA ambLightColor = new ColorRGBA(0f, 0f, 0f, 1f);

        for (int i = 0; i < worldLightList.size(); i++) {
            final Light light = worldLightList.get(i);
            if (light instanceof AmbientLight) {
                ambLightColor.addLocal(light.getColor());
            } else {
                lightList.add(light);
            }
        }

        numLights = lightList.size();
        final int arraySize = Math.max(numLights, 4); // Intel GMA bug
        getMaterialDef().addMaterialParam(VarType.Int, "NumLights", arraySize,
                null);
        setInt("NumLights", arraySize);

        final Uniform lightColor = shader.getUniform("g_LightColor");
        final Uniform lightPos = shader.getUniform("g_LightPosition");
        final Uniform lightDir = shader.getUniform("g_LightDirection");

        lightColor.setVector4Length(arraySize);
        lightPos.setVector4Length(arraySize);
        lightDir.setVector4Length(arraySize);

        final Uniform ambientColor = shader.getUniform("g_AmbientLightColor");
        ambLightColor.a = 1.0f;
        ambientColor.setValue(VarType.Vector4, ambLightColor);

        int i;
        for (i = 0; i < numLights; i++) {
            final Light l = lightList.get(i);
            final ColorRGBA color = l.getColor();
            lightColor.setVector4InArray(color.getRed(), color.getGreen(),
                    color.getBlue(), l.getType().getId(), i);

            switch (l.getType()) {
            case Directional:
                final DirectionalLight dl = (DirectionalLight) l;
                final Vector3f dir = dl.getDirection();
                lightPos.setVector4InArray(dir.getX(), dir.getY(), dir.getZ(),
                        -1, i);
                break;
            case Point:
                final PointLight pl = (PointLight) l;
                final Vector3f pos = pl.getPosition();
                final float invRadius = pl.getInvRadius();
                lightPos.setVector4InArray(pos.getX(), pos.getY(), pos.getZ(),
                        invRadius, i);
                break;
            case Spot:
                final SpotLight sl = (SpotLight) l;
                final Vector3f pos2 = sl.getPosition();
                final Vector3f dir2 = sl.getDirection();
                final float invRange = sl.getInvSpotRange();
                final float spotAngleCos = sl.getPackedAngleCos();

                lightPos.setVector4InArray(pos2.getX(), pos2.getY(),
                        pos2.getZ(), invRange, i);
                lightDir.setVector4InArray(dir2.getX(), dir2.getY(),
                        dir2.getZ(), spotAngleCos, i);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Unknown type of light: " + l.getType());
            }
        }

        for (; i < arraySize; i++) {
            lightColor.setVector4InArray(0, 0, 0, 0, i);
        }

        lightList.clear();
    }
}
