package org.marble.block;

import java.util.Map;
import java.util.Set;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.LightNode;
import com.jme3.scene.Spatial;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.connected.Connected;
import org.marble.entity.connected.Connector;
import org.marble.entity.graphical.Emitter;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Physical;

public class Lantern extends AbstractEntity implements Connected, Graphical,
        Emitter, Physical {
    private final ColorRGBA color;
    private final float radius;
    private Spatial graphicalLantern;
    private RigidBodyControl physicalLantern;
    private ParticleEmitter particles;
    private PointLight light;

    public Lantern() {
        this(new Vector3f(0.5f, 0.5f, 0.5f));
    }

    public Lantern(final Vector3f colorVector) {
        this(colorVector, 32);
    }

    public Lantern(final Vector3f colorVector, final float radius) {
        color = new ColorRGBA(colorVector.x, colorVector.y, colorVector.z, 1);
        this.radius = radius;
    }

    @Override
    public RigidBodyControl getBody() {
        return physicalLantern;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        // TODO Add connectors
        return ImmutableMap.of();
    }

    @Override
    public void initialize(final Game game) {
        super.initialize(game);
        final AssetManager assetManager = game.getAssetManager();
        graphicalLantern = assetManager.loadModel("Models/lantern.obj");
        graphicalLantern.setMaterial(assetManager
                .loadMaterial("Materials/Mineral/Concrete.j3m"));
        getSpatial().attachChild(graphicalLantern);

        final ColorRGBA flameColor = color.clone();
        final float maxComponent =
                Math.max(Math.max(flameColor.a, flameColor.r),
                        Math.max(flameColor.g, flameColor.b));
        flameColor.multLocal(1 / maxComponent);
        final ColorRGBA fumeColor = flameColor.clone();
        fumeColor.a = 0;

        light = new PointLight();
        light.setColor(color);
        light.setRadius(radius);
        final LightNode lightNode = new LightNode("lantern light", light);
        lightNode.setLocalTranslation(0, 0, 0.5f);
        getSpatial().attachChild(lightNode);

        particles =
                new ParticleEmitter("emitter", ParticleMesh.Type.Triangle, 8);
        final Material material =
                new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                assetManager.loadTexture("Textures/flare.png"));
        material.getAdditionalRenderState().setBlendMode(BlendMode.Additive);
        particles.setMaterial(material);
        particles.setStartColor(flameColor);
        particles.setEndColor(fumeColor);

        particles.getParticleInfluencer().setInitialVelocity(
                new Vector3f(0, 0, 8f));
        particles.getParticleInfluencer().setVelocityVariation(0.1f);
        particles.setStartSize(1.0f);
        particles.setEndSize(0.25f);
        particles.setGravity(0, 0, 0);
        particles.setLowLife(0.1f);
        particles.setHighLife(0.2f);
        particles.setLocalTranslation(0, 0, 0.5f);
        particles.updateLogicalState(20);
        getSpatial().attachChild(particles);

        physicalLantern =
                new RigidBodyControl(new BoxCollisionShape(new Vector3f(0.5f,
                        0.5f, 1)), 0);
        getSpatial().addControl(physicalLantern);
    }

    @Override
    public Set<Light> getLights() {
        return ImmutableSet.<Light> of(light);
    }
}
