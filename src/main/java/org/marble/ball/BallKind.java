package org.marble.ball;

import java.util.concurrent.Callable;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.texture.TextureCubeMap;

import org.marble.frp.FRPUtils;
import org.marble.frp.ReactiveListener;
import org.marble.graphics.EnvironmentNode;
import org.marble.util.Physics;

/**
 * A kind of ball material.
 */
public enum BallKind {
    /** A stone ball: heavy and slow */
    Stone(8, 0.5f, 0.2f) {
        @Override
        public Material createMaterial(final AssetManager assetManager,
                final Callable<EnvironmentNode> getEnvironment)
                throws Exception {
            return assetManager.loadMaterial("Materials/Mineral/Stone.j3m");
        }

    },

    /** A wooden ball: light and agile */

    Wood(4, 1, 0.4f) {

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
    Fabric(2, 2.0f, 0.8f) {

        @Override
        public Material createMaterial(final AssetManager assetManager,
                final Callable<EnvironmentNode> getEnvironment)
                throws Exception {
            return assetManager.loadMaterial("Materials/Organic/Fabric.j3m");
        }

    },

    /** An easily controlled ball that might break when moved too quickly */

    Glass(6, 1.3f, 0.3f) {

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
    Mercury(16, 0.25f, 0.1f) {

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

    private final float mass;

    private final float force;

    private final float linearDamping;

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

    private BallKind(final float mass, final float maxAngle,
            final float linearDamping) {
        this.mass = mass;
        this.linearDamping = linearDamping;
        force =
                (float) (-Physics.GRAVITY.getZ() * mass
                        * Math.sin(Math.atan(maxAngle)) / Math.cos(Math
                        .atan(maxAngle)));
    }

    public float getMass() {
        return mass;
    }

    public float getForce() {
        return force;

    }

    public float getLinearDamping() {
        return linearDamping;
    }

    private static void randomize(final Vector3f vec) {
        vec.setX((float) Math.random());
        vec.setY((float) Math.random());
        vec.setZ((float) Math.random());
    }
}
