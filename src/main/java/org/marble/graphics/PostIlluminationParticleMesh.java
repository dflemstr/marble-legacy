package org.marble.graphics;

import com.ardor3d.extension.effect.particle.ParticleMesh;
import com.ardor3d.extension.effect.particle.ParticleSystem;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Mesh;

import org.marble.graphics.scene.PostIllumination;

public class PostIlluminationParticleMesh extends ParticleMesh implements
        PostIllumination {

    public PostIlluminationParticleMesh() {
    }

    public PostIlluminationParticleMesh(final String name,
            final int numParticles) {
        super(name, numParticles);
    }

    public PostIlluminationParticleMesh(final String name,
            final int numParticles, final ParticleSystem.ParticleType type) {
        super(name, numParticles, type);
    }

    public PostIlluminationParticleMesh(final String name, final Mesh sourceMesh) {
        super(name, 0);
    }

    @Override
    public void draw(final Renderer r) {
        // Do nothing
    }

    @Override
    public void postIllumination(final Renderer r) {
        super.draw(r);
    }

}
