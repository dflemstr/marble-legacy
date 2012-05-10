package org.marble.engine;

import com.jme3.light.Light;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.JmeContext;

import org.marble.entity.graphical.Emitter;
import org.marble.entity.graphical.Graphical;

/**
 * The Ardor3D-based graphics engine.
 */
public class GraphicsEngine extends Engine<Graphical> {
    private Camera camera;
    private final JmeContext context;
    private final Node guiNode = new Node("gui");
    private ViewPort guiViewPort;
    private Renderer renderer;

    private RenderManager renderManager;

    private final Node rootNode = new Node("root");

    private ViewPort viewPort;

    public GraphicsEngine(final JmeContext context) {
        super(Graphical.class);
        this.context = context;
    }

    /**
     * @return the camera
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * @return the guiNode
     */
    public Node getGuiNode() {
        return guiNode;
    }

    /**
     * @return the guiViewPort
     */
    public ViewPort getGuiViewPort() {
        return guiViewPort;
    }

    public RenderManager getRenderManager() {
        return renderManager;
    }

    /**
     * @return the rootNode
     */
    public Node getRootNode() {
        return rootNode;
    }

    public ViewPort getViewPort() {
        return viewPort;
    }

    @Override
    public void initialize() {
        renderer = context.getRenderer();

        renderManager = new RenderManager(renderer);
        renderManager.setTimer(context.getTimer());

        camera =
                new Camera(context.getSettings().getWidth(), context
                        .getSettings().getHeight());
        camera.setFrustumPerspective(45f,
                (float) camera.getWidth() / camera.getHeight(), 1f, 1000f);
        camera.setLocation(new Vector3f(0, -10, 0));
        camera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Z);

        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);

        viewPort = renderManager.createMainView("Default", camera);
        viewPort.setClearFlags(true, true, true);
        viewPort.attachScene(rootNode);

        final Camera guiCam =
                new Camera(context.getSettings().getWidth(), context
                        .getSettings().getHeight());
        guiViewPort = renderManager.createPostView("GUI Default", guiCam);
        guiViewPort.setClearFlags(false, false, false);
        guiViewPort.attachScene(guiNode);
    }

    public void reshape(final int width, final int height) {
        renderManager.notifyReshape(width, height);
    }

    @Override
    public void resume() {
        context.setAutoFlushFrames(true);
    }

    @Override
    public void suspend() {
        context.setAutoFlushFrames(false);
    }

    @Override
    public void update(final float timePerFrame) {
        rootNode.updateLogicalState(timePerFrame);
        guiNode.updateLogicalState(timePerFrame);

        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        renderManager.render(timePerFrame, context.isRenderable());
    }

    @Override
    protected void entityAdded(final Graphical entity) {
        rootNode.attachChild(entity.getSpatial());

        if (entity instanceof Emitter) {
            for (final Light light : ((Emitter) entity).getLights()) {
                rootNode.addLight(light);
            }
        }
    }

    @Override
    protected void entityRemoved(final Graphical entity) {
        rootNode.detachChild(entity.getSpatial());

        if (entity instanceof Emitter) {
            for (final Light light : ((Emitter) entity).getLights()) {
                rootNode.removeLight(light);
            }
        }
    }
}
