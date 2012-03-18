package org.marble.engine;

import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.ReadOnlyTimer;

import org.marble.entity.Graphical;

/**
 * The Ardor3D-based graphics engine.
 */
public class GraphicsEngine extends Engine<Graphical> {
    private final Node rootNode = new Node();
    private final NativeCanvas canvas;
    private ZBufferState zbuffer;
    private LightState lighting;
    private WireframeState wireframeState;

    /**
     * Creates a new graphics engine.
     *
     * @param canvas
     *            The canvas to render to.
     */
    public GraphicsEngine(final NativeCanvas canvas) {
        super(Graphical.class);
        this.canvas = canvas;
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    protected void entityAdded(final Graphical entity) {
        rootNode.attachChild(entity.getSpatial());
    }

    @Override
    protected void entityRemoved(final Graphical entity) {
        rootNode.detachChild(entity.getSpatial());
    }

    /**
     * The canvas that is being rendered to.
     */
    public NativeCanvas getCanvas() {
        return canvas;
    }

    /**
     * The lighting system in use.
     */
    public LightState getLighting() {
        return lighting;
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

        rootNode.getSceneHints().setRenderBucketType(RenderBucketType.Opaque);

        zbuffer = new ZBufferState();
        zbuffer.setEnabled(true);
        zbuffer.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        rootNode.setRenderState(zbuffer);

        lighting = new LightState();
        lighting.setEnabled(true);
        rootNode.setRenderState(lighting);

        wireframeState = new WireframeState();
        wireframeState.setEnabled(true);
        rootNode.setRenderState(wireframeState);

        rootNode.updateGeometricState(0);
    }

    @Override
    public boolean update(final ReadOnlyTimer timer) {
        rootNode.updateGeometricState(timer.getTimePerFrame(), true);
        return !canvas.isClosing();
    }
}
