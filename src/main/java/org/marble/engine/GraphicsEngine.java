package org.marble.engine;

import java.util.Map;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;

import com.google.common.collect.Maps;

import org.marble.entity.Emitter;
import org.marble.entity.Graphical;
import org.marble.graphics.scene.EntityController;

/**
 * The Ardor3D-based graphics engine.
 */
public class GraphicsEngine extends Engine<Graphical> {
    private final Node rootNode = new Node();
    private final DisplaySettings displaySettings;
    private final NativeCanvas canvas;
    private ZBufferState zbuffer;
    private LightState lighting;
    private CullState culling;
    private TextureState textures;

    private BasicPassManager passes;

    private final Map<Graphical, EntityController> controllers = Maps
            .newIdentityHashMap();

    /**
     * Creates a new graphics engine.
     * 
     * @param canvas
     *            The canvas to render to.
     */
    public GraphicsEngine(final NativeCanvas canvas,
            final DisplaySettings displaySettings) {
        super(Graphical.class);
        this.canvas = canvas;
        this.displaySettings = displaySettings;
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    /**
     * The canvas that is being rendered to.
     */
    public NativeCanvas getCanvas() {
        return canvas;
    }

    public DisplaySettings getDisplaySettings() {
        return displaySettings;
    }

    /**
     * The lighting system in use.
     */
    public LightState getLighting() {
        return lighting;
    }

    public BasicPassManager getPasses() {
        return passes;
    }

    /**
     * The root node; all graphical nodes in the game must be sub-nodes of this
     * node.
     */
    public Node getRootNode() {
        return rootNode;
    }

    /**
     * The Z-buffer in use.
     */
    public ZBufferState getZBuffer() {
        return zbuffer;
    }

    @Override
    public void initialize() {
        canvas.setTitle("Marble");
        canvas.getCanvasRenderer().getCamera().setFrustumFar(512.0);
        canvas.getCanvasRenderer().getCamera().setFrustumNear(1.0);

        rootNode.getSceneHints().setRenderBucketType(RenderBucketType.Opaque);

        zbuffer = new ZBufferState();
        zbuffer.setEnabled(true);
        zbuffer.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        rootNode.setRenderState(zbuffer);

        lighting = new LightState();
        lighting.setEnabled(true);
        rootNode.setRenderState(lighting);

        /*
         * final WireframeState wireframeState = new WireframeState();
         * wireframeState.setEnabled(true);
         * rootNode.setRenderState(wireframeState);
         */

        culling = new CullState();
        culling.setEnabled(true);
        culling.setCullFace(CullState.Face.Back);
        rootNode.setRenderState(culling);

        textures = new TextureState();
        textures.setEnabled(true);
        textures.setTexture(TextureManager.load("texture/missing.png",
                Texture.MinificationFilter.Trilinear, false));
        rootNode.setRenderState(textures);

        passes = new BasicPassManager();

        rootNode.updateGeometricState(0);
    }

    @Override
    public boolean update(final ReadOnlyTimer timer) {
        rootNode.updateGeometricState(timer.getTimePerFrame(), true);
        passes.updatePasses(timer.getTimePerFrame());
        return !canvas.isClosing();
    }

    @Override
    protected void entityAdded(final Graphical entity) {
        final Spatial spatial = entity.getSpatial();
        final EntityController controller = new EntityController(entity);

        if (entity instanceof Emitter) {
            lighting.attach(((Emitter) entity).getLight());
        }

        spatial.addController(controller);
        controllers.put(entity, controller);
        rootNode.attachChild(spatial);
    }

    @Override
    protected void entityRemoved(final Graphical entity) {
        final Spatial spatial = entity.getSpatial();
        final EntityController controller = controllers.remove(entity);

        if (entity instanceof Emitter) {
            lighting.detach(((Emitter) entity).getLight());
        }

        spatial.removeController(controller);
        rootNode.detachChild(spatial);
    }
}
