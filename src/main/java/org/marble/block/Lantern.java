package org.marble.block;

import java.util.Map;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.extension.effect.particle.ParticleFactory;
import com.ardor3d.extension.effect.particle.ParticleSystem;
import com.ardor3d.extension.model.obj.ObjImporter;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.WrapMode;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.light.Light;
import com.ardor3d.light.PointLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyEventListener;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.util.TextureManager;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.Connected;
import org.marble.entity.Connector;
import org.marble.entity.Emitter;
import org.marble.entity.Graphical;
import org.marble.entity.Physical;

public class Lantern extends AbstractEntity implements Connected, Graphical,
        Physical, Emitter {
    private final Node graphicalLantern;
    private final RigidBody physicalLantern;
    private final ParticleSystem particles;
    private final PointLight light;

    public Lantern() {
        this(new Vector3d(0.5, 0.5, 0.5));
    }

    public Lantern(final Vector3d colorVector) {
        graphicalLantern = new ObjImporter().load("lantern.obj").getScene();

        final CollisionShape geometricalLantern =
                new BoxShape(new Vector3f(0.5f, 0.5f, 1));
        final ColorRGBA color =
                new ColorRGBA((float) colorVector.x, (float) colorVector.y,
                        (float) colorVector.z, 1);
        final float colorScale =
                1.0f / Math.max(Math.max(color.getRed(), color.getGreen()),
                        color.getBlue());
        final ColorRGBA saturatedColor = color.multiply(colorScale, null);
        final ColorRGBA alphaColor = color.clone();
        alphaColor.setAlpha(0);

        light = new PointLight();
        light.setAmbient(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
        light.setDiffuse(color);
        light.setSpecular(saturatedColor);
        light.setEnabled(true);
        light.setLocation(0, 0, 1);
        graphicalLantern.setListener(new DirtyEventListener() {
            private final Vector3 lightPos = new Vector3();

            @Override
            public boolean spatialDirty(final Spatial spatial,
                    final DirtyType dirtyType) {
                return false;
            }

            @Override
            public boolean spatialClean(final Spatial spatial,
                    final DirtyType dirtyType) {
                if (dirtyType == DirtyType.Transform) {
                    lightPos.set(graphicalLantern.getWorldTranslation());
                    lightPos.addLocal(0, 0, 1);
                    light.setLocation(lightPos);
                }
                return false;
            }

        });

        particles = ParticleFactory.buildParticles("flame", 8);
        particles.setEmissionDirection(new Vector3(0, 0, 1));
        particles.setInitialVelocity(0.006);
        particles.setStartSize(0.5);
        particles.setEndSize(0.125);
        particles.setMinimumLifeTime(100);
        particles.setMaximumLifeTime(200);
        particles.setStartColor(saturatedColor);
        particles.setEndColor(alphaColor);
        particles.setMaximumAngle(15 * MathUtils.DEG_TO_RAD);
        particles.setTranslation(0, 0, 0.5);

        final BlendState blend = new BlendState();
        blend.setBlendEnabled(true);
        blend.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        blend.setDestinationFunction(BlendState.DestinationFunction.One);
        particles.setRenderState(blend);

        final TextureState ts = new TextureState();
        ts.setTexture(TextureManager.load("flare-small.png",
                Texture.MinificationFilter.Trilinear,
                TextureStoreFormat.GuessCompressedFormat, true));
        ts.getTexture().setWrap(WrapMode.BorderClamp);
        ts.setEnabled(true);
        particles.setRenderState(ts);

        final ZBufferState zstate = new ZBufferState();
        zstate.setWritable(false);
        particles.setRenderState(zstate);

        particles.getParticleGeometry().setModelBound(new BoundingBox());

        graphicalLantern.attachChild(particles);
        particles.warmUp(60);
        graphicalLantern.updateWorldTransform(true);

        final Vector3f inertia = new Vector3f(0, 0, 0);
        geometricalLantern.calculateLocalInertia(0.0f, inertia);

        final RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo(0.0f, new DefaultMotionState(),
                        geometricalLantern, inertia);
        physicalLantern = new RigidBody(info);
    }

    @Override
    public void initialize(final Game game) {
        particles.getParticleController().setUpdateOnlyInView(true);
        particles.getParticleController().setViewCamera(
                game.getGraphicsEngine().getCanvas().getCanvasRenderer()
                        .getCamera());
    }

    @Override
    public Iterable<ActionInterface> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public RigidBody getBody() {
        return physicalLantern;
    }

    @Override
    public Spatial getSpatial() {
        return graphicalLantern;
    }

    @Override
    public Map<String, Connector> getConnectors() {
        // TODO Add connectors
        return ImmutableMap.of();
    }

    @Override
    public Light getLight() {
        return light;
    }

}
