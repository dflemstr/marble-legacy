package org.marble.ball;

import java.util.concurrent.Callable;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.texture.TextureCubeMap;

import org.marble.frp.FRPUtils;
import org.marble.frp.ReactiveListener;
import org.marble.graphics.EnvironmentNode;

/**
 * A kind of ball material.
 */
public enum BallKind {
    /** A stone ball: heavy and slow */
    Stone(6.0f, 0.5f, 0.5f, 4.0f, 0.5f, 0) {
        @Override
        public Material createMaterial(final AssetManager assetManager,
                final Callable<EnvironmentNode> getEnvironment)
                throws Exception {
            return assetManager.loadMaterial("Materials/Mineral/Stone.j3m");
        }

    },

    /** A wooden ball: light and agile */
    Wood(1.0f, 1.0f, 1.0f, 0.8f, 0.5f, 0) {
        @Override
        public Material createMaterial(final AssetManager assetManager,
                final Callable<EnvironmentNode> getEnvironment)
                throws Exception {

            final Material material =
                    assetManager.loadMaterial("Materials/Organic/Wood.j3m");

            final Vector3f vec = new Vector3f();

            // The trunkCenter vectors define a line that is the center of the
            // trunk that our wood was cut from - all the "rings" will be around
            // this axis.
            randomize(vec);
            // material.setVector3("TrunkCenter1", vec);

            randomize(vec);
            // material.setVector3("TrunkCenter2", vec);

            // The noiseSeed vector seeds the random noise generator. The
            // generator has a period of 289.
            randomize(vec);
            vec.multLocal(289);
            material.setVector3("NoiseSeed", vec);

            // The variation is a value between 0.0 and 1.0 that determines
            // which column of the wood gradient texture that is used for
            // tinting the material.
            material.setFloat("Variation", (float) Math.random());
            return material;
        }
    },

    /** A fabric ball: very light and flimsy */
    Fabric(0.25f, 2.0f, 2.0f, 0.2f, 0.5f, 0) {

        @Override
        public Material createMaterial(final AssetManager assetManager,
                final Callable<EnvironmentNode> getEnvironment)
                throws Exception {
            return assetManager.loadMaterial("Materials/Organic/Fabric.j3m");
        }

    },

    /** An easily controlled ball that might break when moved too quickly */
    Glass(2.0f, 1.0f, 0.5f, 1.0f, 0.5f, 0) {

        @Override
        public Material createMaterial(final AssetManager assetManager,
                final Callable<EnvironmentNode> getEnvironment)
                throws Exception {
            final Material material =
                    assetManager.loadMaterial("Materials/Mineral/Glass.j3m");
            FRPUtils.addAndCallReactiveListener(getEnvironment.call()
                    .getEnvironment(), new ReactiveListener<TextureCubeMap>() {

                @Override
                public void valueChanged(final TextureCubeMap value) {
                    material.setTexture("EnvironmentMap", value);
                }
            });
            return material;
        }

    },

    /**
     * A ball that leaves a trail of mercury as it moves, slowly growing smaller
     */
    Mercury(8.0f, 2.0f, 16.0f, 8.0f, 0.5f, 0) {

        @Override
        public Material createMaterial(final AssetManager assetManager,
                final Callable<EnvironmentNode> getEnvironment)
                throws Exception {
            final Material material =
                    assetManager.loadMaterial("Materials/Metal/Mercury.j3m");
            FRPUtils.addAndCallReactiveListener(getEnvironment.call()
                    .getEnvironment(), new ReactiveListener<TextureCubeMap>() {

                @Override
                public void valueChanged(final TextureCubeMap value) {
                    material.setTexture("EnvironmentMap", value);
                }
            });
            return material;
        }

    };

    private static final float MASS_SCALE = 4.0f;
    private static final float STABILITY_SCALE = 4.0f;

    private final float mass;
    private final float linearDamping;
    private final float angularDamping;
    private final float stability;
    private final float friction;
    private final float restitution;

    private BallKind(final float mass, final float linearDamping,
            final float angularDamping, final float stability,
            final float friction, final float restitution) {
        this.mass = mass * MASS_SCALE;
        this.linearDamping = linearDamping;
        this.angularDamping = angularDamping;
        this.stability = stability * STABILITY_SCALE;
        this.friction = friction;
        this.restitution = restitution;
    }

    /**
     * Creates a graphical material for this kind of ball.
     * 
     * @param assetManager
     *            The asset manager to load resources from.
     * @param getEnvironment
     *            A closure that retrieves an environment node, if this material
     *            requires it. If no environment is required, the closure won't
     *            be called.
     * @return The constructed material.
     * @throws Exception
     *             if the getEnvironment closure fails, or a resource loading
     *             exception is thrown.
     */
    public abstract Material createMaterial(final AssetManager assetManager,
            final Callable<EnvironmentNode> getEnvironment) throws Exception;

    public float getAngularDamping() {
        return angularDamping;
    }

    public float getLinearDamping() {
        return linearDamping;
    }

    public float getMass() {
        return mass;
    }

    public float getStability() {
        return stability;
    }

    public float getFriction() {
        return friction;
    }

    public float getRestitution() {
        return restitution;
    }

    private static void randomize(final Vector3f vec) {
        vec.setX((float) Math.random());
        vec.setY((float) Math.random());
        vec.setZ((float) Math.random());
    }
}
