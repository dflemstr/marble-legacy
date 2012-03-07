package org.marble.engine;

import org.marble.entity.Graphical;

import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.ReadOnlyTimer;

/**
 * The Ardor3D-based graphics engine.
 */
public class GraphicsEngine extends Engine<Graphical> {
    private final Node rootNode = new Node();
    private final NativeCanvas canvas;
    private ZBufferState zbuffer;
    private LightState lighting;

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
        this.rootNode.attachChild(entity.getSpatial());
    }

    @Override
    protected void entityRemoved(final Graphical entity) {
        this.rootNode.detachChild(entity.getSpatial());
    }

    /**
     * The canvas that is being rendered to.
     */
    public NativeCanvas getCanvas() {
        return this.canvas;
    }

    /**
     * The lighting system in use.
     */
    public LightState getLighting() {
        return this.lighting;
    }

    /**
     * The root node; all graphical nodes in the game must be sub-nodes of this
     * node.
     */
    public Node getRootNode() {
        return this.rootNode;
    }

    /**
     * The Z-buffer in use.
     */
    public ZBufferState getZBuffer() {
        return this.zbuffer;
    }

    @Override
    public void initialize() {
        this.canvas.setTitle("Marble");

        this.rootNode.getSceneHints().setRenderBucketType(
                RenderBucketType.Opaque);

        this.zbuffer = new ZBufferState();
        this.zbuffer.setEnabled(true);
        this.zbuffer.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        this.rootNode.setRenderState(this.zbuffer);

        this.lighting = new LightState();
        this.lighting.setEnabled(true);
        this.rootNode.setRenderState(this.lighting);

        this.rootNode.updateGeometricState(0);
    }

    @Override
    public boolean update(final ReadOnlyTimer timer) {
        this.rootNode.updateGeometricState(timer.getTimePerFrame(), true);
        return !this.canvas.isClosing();
    }
}
