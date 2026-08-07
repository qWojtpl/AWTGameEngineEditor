package pl.AWTGameEngineEditor.util;

import com.intellij.ui.components.JBSlider;
import com.intellij.util.ui.FormBuilder;
import com.jogamp.opengl.awt.GLCanvas;
import pl.AWTGameEngine.Dependencies;
import pl.AWTGameEngine.engine.enums.RenderEngine;
import pl.AWTGameEngine.engine.helpers.RotationHelper;
import pl.AWTGameEngine.engine.panels.PanelGL;
import pl.AWTGameEngine.objects.render.Camera;
import pl.AWTGameEngine.objects.transform.TransformSet;
import pl.AWTGameEngine.windows.Window;

import javax.swing.*;

public class Preview {

    private Window window;

    public void createPreview(FormBuilder builder, String scenePath) {
        window = (pl.AWTGameEngine.windows.Window) Dependencies.getWindowsManager().createNestedEditorWindow(
                Dependencies.getResourceManager().getResourceAsStream(scenePath),
                scenePath, RenderEngine.OPENGL);
        window.getRenderLoop().setTargetFps(60);
        window.getUpdateLoop().setTargetFps(60);
        window.getUpdateLoop().start();
        JPanel panel = new JPanel();
        GLCanvas canvas = ((PanelGL) window.getCurrentScene().getPanel()).getGlCanvas();
        canvas.setSize(320, 180);
        panel.add(canvas);
        builder.addSeparator();
        builder.addComponent(panel);
        Camera camera = window.getCurrentScene().getPanel().getCamera();
        JBSlider distanceSlider = new JBSlider();
        distanceSlider.setMaximum(3000);
        distanceSlider.setMinimum(100);
        distanceSlider.setValue(0);
        JBSlider rotationSlider = new JBSlider();
        rotationSlider.setMinimum(0);
        rotationSlider.setMaximum(360);
        rotationSlider.setValue(0);
        distanceSlider.addChangeListener((v) -> {
            camera.setZ(distanceSlider.getValue());
            updateRotation(camera, distanceSlider, rotationSlider);
        });
        rotationSlider.addChangeListener((v) -> updateRotation(camera, distanceSlider, rotationSlider));
        updateRotation(camera, distanceSlider, rotationSlider);
        builder.addLabeledComponent("Preview distance", distanceSlider);
        builder.addLabeledComponent("Preview rotation", rotationSlider);
    }

    public void disposePreview() {
        if(window == null) {
            return;
        }
        window.getUpdateLoop().kill();
        window.getRenderLoop().kill();
        window.unloadScenes();
        Dependencies.getWindowsManager().removeWindow(window);
        System.gc();
        window = null;
    }

    private void updateRotation(Camera camera, JBSlider distanceSlider, JBSlider rotationSlider) {
        double[] camPos = RotationHelper.radiusLook(0, 0, 0, distanceSlider.getValue(), 0, rotationSlider.getValue());
        camera.setPosition(new TransformSet(camPos[0], camPos[1], camPos[2]));
        double[] look = RotationHelper.lookAt(camera.getX(), camera.getY(), camera.getZ(), 0, 0, 0);
        camera.setRotation(new TransformSet(look[0], look[1], look[2]));
    }

    public Window getWindow() {
        return this.window;
    }

}
