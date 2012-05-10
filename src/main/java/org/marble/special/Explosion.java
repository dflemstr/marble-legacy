package org.marble.special;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.graphical.Graphical;

public class Explosion extends AbstractEntity implements Graphical {
    private final ColorRGBA color = new ColorRGBA();
    private boolean hasEmitted = false;
    private ParticleEmitter particles;

    public Explosion(final ColorRGBA color) {
        this.color.set(color);
    }

    @Override
    public void initialize(final Game game) throws Exception {
        super.initialize(game);
        particles = makeExplosionParticles(game.getAssetManager(), color);
        getSpatial().attachChild(particles);
    }

    @Override
    public void update(final float tpf) {
        if (!hasEmitted) {
            particles.emitAllParticles();
            hasEmitted = true;
        }
        if (particles.getNumVisibleParticles() < 2) {
            game.getEntityManager().removeEntity(this);
        }
    }

    private ParticleEmitter makeExplosionParticles(
            final AssetManager assetManager, final ColorRGBA color) {

        final ParticleEmitter particles =
                new ParticleEmitter("emitter", ParticleMesh.Type.Triangle, 64);
        final Material material =
                new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                assetManager.loadTexture("Textures/flare.png"));
        particles.setMaterial(material);

        particles.setStartColor(color);
        particles.setEndColor(color);

        particles.getParticleInfluencer().setInitialVelocity(
                new Vector3f(0.5f, 0, 0));
        particles.getParticleInfluencer().setVelocityVariation(1);
        particles.setStartSize(0.2f);
        particles.setEndSize(0.1f);
        particles.setGravity(0, 0, 0);
        particles.setLowLife(0.5f);
        particles.setHighLife(1f);
        particles.setParticlesPerSec(0);
        return particles;
    }
}
