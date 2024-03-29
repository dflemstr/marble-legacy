package org.marble.special;

import java.util.Set;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.LightNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

import com.google.common.collect.ImmutableSet;

import org.marble.Game;
import org.marble.ball.PlayerBall;
import org.marble.entity.AbstractEntity;
import org.marble.entity.graphical.Emitter;
import org.marble.entity.graphical.Graphical;
import org.marble.entity.physical.Collidable;
import org.marble.entity.physical.Physical;
import org.marble.util.Direction;

public class WinBlock extends AbstractEntity implements Graphical, Physical,
        Collidable, Emitter {
    private Game game;

    private Node graphicalBox;
    private Node graphicalSpinner;
    private PointLight light1, light2;
    private RigidBodyControl physicalBox;

    @Override
    public RigidBodyControl getBody() {
        return physicalBox;
    }

    @Override
    public Set<Light> getLights() {
        return ImmutableSet.<Light> of(light1, light2);
    }

    @Override
    public void handleCollisionWith(final Physical other,
            final PhysicsCollisionEvent event) {
        if (other instanceof PlayerBall) {
            game.winLevel();
        }
    }

    @Override
    public void initialize(final Game game) {
        this.game = game;
        final AssetManager assetManager = game.getAssetManager();
        graphicalBox = new Node("invisible");
        getSpatial().attachChild(graphicalBox);

        final ColorRGBA color = ColorRGBA.Green.mult(0.66f);

        physicalBox =
                new RigidBodyControl(new BoxCollisionShape(new Vector3f(0.5f,
                        0.5f, 0.5f)), 0);
        getSpatial().addControl(physicalBox);

        graphicalSpinner = new Node("spinner");
        graphicalSpinner.addControl(new VerticalSpinner(FastMath.PI));

        final Node emitter1 = new Node("emitter 1");
        emitter1.setLocalTranslation(FastMath.sqrt(2) / 2, 0, -0.4f);

        final ParticleEmitter particles1 = makeWinParticles(assetManager);
        particles1.updateLogicalState(20);
        emitter1.attachChild(particles1);

        light1 = new PointLight();
        light1.setColor(color);
        light1.setRadius(8);
        final LightNode lightNode1 = new LightNode("winning light 1", light1);
        lightNode1.setLocalTranslation(0, 0, 0.5f);
        emitter1.attachChild(lightNode1);

        graphicalSpinner.attachChild(emitter1);

        final Node emitter2 = new Node("emitter 1");
        emitter2.setLocalTranslation(-FastMath.sqrt(2) / 2, 0, -0.4f);

        final ParticleEmitter particles2 = makeWinParticles(assetManager);
        particles2.updateLogicalState(20);
        emitter2.attachChild(particles2);

        light2 = new PointLight();
        light2.setColor(color);
        light2.setRadius(8);
        final LightNode lightNode2 = new LightNode("winning light 1", light2);
        lightNode2.setLocalTranslation(0, 0, 0.5f);
        emitter2.attachChild(lightNode2);

        graphicalSpinner.attachChild(emitter2);

        getSpatial().attachChild(graphicalSpinner);
    }

    private ParticleEmitter makeWinParticles(final AssetManager assetManager) {

        final ParticleEmitter particles =
                new ParticleEmitter("emitter", ParticleMesh.Type.Triangle, 16);
        final Material material =
                new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                assetManager.loadTexture("Textures/flare.png"));
        particles.setMaterial(material);
        particles.setStartColor(ColorRGBA.Green);
        particles.setEndColor(ColorRGBA.Yellow);

        particles.getParticleInfluencer().setInitialVelocity(
                new Vector3f(0, 0, 2f));
        particles.getParticleInfluencer().setVelocityVariation(0.1f);
        particles.setStartSize(0.2f);
        particles.setEndSize(0.1f);
        particles.setGravity(0, 0, 0);
        particles.setLowLife(0.25f);
        particles.setHighLife(0.5f);
        return particles;
    }

    private final class VerticalSpinner extends AbstractControl {
        private float angle;
        private final float radialVelocity;
        private final Quaternion rotation = new Quaternion();

        public VerticalSpinner(final float radialVelocity) {
            this(radialVelocity, 0);
        }

        public VerticalSpinner(final float radialVelocity, final float angle) {
            this.radialVelocity = radialVelocity;
            this.angle = angle;
        }

        @Override
        public Control cloneForSpatial(final Spatial spatial) {
            return new VerticalSpinner(angle, radialVelocity);
        }

        @Override
        protected void controlRender(final RenderManager rm, final ViewPort vp) {
            // Do nothing
        }

        @Override
        protected void controlUpdate(final float tpf) {
            angle += radialVelocity * tpf;
            rotation.fromAngleAxis(angle, Direction.Up.getPhysicalDirection());
            spatial.setLocalRotation(rotation);
        }
    }
}
