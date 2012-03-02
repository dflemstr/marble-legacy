package org.marble;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.light.PointLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.ReadOnlyTimer;

public class Game {
    private final Node rootNode = new Node();
    private final NativeCanvas canvas;
    @SuppressWarnings("unused")
    private final LogicalLayer logicalLayer;
    @SuppressWarnings("unused")
    private final MouseManager mouseManager;

    private ZBufferState zbuffer;
    private LightState light;

    /** Keep a reference to the box to be able to rotate it each frame. */
    private Mesh box;

    /** Rotation matrix for the spinning box. */
    private final Matrix3 rotate = new Matrix3();

    /** Angle of rotation for the box. */
    private double angle = 0;

    /** Axis to rotate the box around. */
    private final Vector3 axis = new Vector3(1, 1, 0.5f).normalizeLocal();

    public Game(final NativeCanvas canvas, final LogicalLayer logicalLayer,
            final MouseManager mouseManager) {
        this.canvas = canvas;
        this.logicalLayer = logicalLayer;
        this.mouseManager = mouseManager;
    }

    public Node getRootNode() {
        return this.rootNode;
    }

    public void initialize() {
        this.canvas.setTitle("Marble");

        this.zbuffer = new ZBufferState();
        this.zbuffer.setEnabled(true);
        this.zbuffer.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        this.rootNode.setRenderState(this.zbuffer);

        final PointLight plight = new PointLight();
        plight.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        plight.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        plight.setLocation(new Vector3(100, 100, 100));
        plight.setEnabled(true);

        this.light = new LightState();
        this.light.setEnabled(true);
        this.light.attach(plight);
        this.rootNode.setRenderState(this.light);

        this.box = new Box("Box", new Vector3(0, 0, 0), 5, 5, 5);
        this.box.setModelBound(new BoundingBox());
        this.box.setTranslation(0, 0, -15);
        this.box.setRandomColors();
        this.rootNode.attachChild(this.box);
    }

    public boolean update(final ReadOnlyTimer timer) {
        this.angle += timer.getTimePerFrame() * 50;
        this.angle %= 360;

        this.rotate.fromAngleNormalAxis(this.angle * MathUtils.DEG_TO_RAD,
                this.axis);
        this.box.setRotation(this.rotate);
        return true;
    }
}
