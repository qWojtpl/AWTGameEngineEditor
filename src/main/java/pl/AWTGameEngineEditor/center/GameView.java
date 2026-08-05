package pl.AWTGameEngineEditor.center;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.jogamp.opengl.awt.GLCanvas;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.AWTGameEngine.Dependencies;
import pl.AWTGameEngine.engine.WaitForSeconds;
import pl.AWTGameEngine.engine.enums.KeyCode;
import pl.AWTGameEngine.engine.enums.RenderEngine;
import pl.AWTGameEngine.engine.panels.PanelGL;
import pl.AWTGameEngine.objects.render.Camera;
import pl.AWTGameEngine.objects.transform.TransformSet;
import pl.AWTGameEngine.windows.BaseWindow;
import pl.AWTGameEngine.windows.WindowsManager;
import pl.AWTGameEngineEditor.hierarchy.ObjectHierarchy;

import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;

public class GameView implements FileEditor {

    private static GameView instance;
    private final VirtualFile file;
    private final JPanel panel = new JPanel();
    private final BaseWindow window;

    Camera camera;
    double forward = 0, right = 0, up = 0;
    double speed = 2;

    public GameView(VirtualFile file) {
        this.file = file;
        instance = this;
        WindowsManager manager = Dependencies.getWindowsManager();
        window = manager.createWindow("scenes/performance/vehicle.xml", RenderEngine.OPENGL, true);
        window.getPhysicsLoop().kill();
        window.getUpdateLoop().kill();
        window.getNetLoop().kill();
        window.getGUILoop().kill();
        window.setVisible(false);
        GLCanvas glCanvas = ((PanelGL) window.getCurrentScene().getPanel()).getGlCanvas();
        panel.add(glCanvas);
        ObjectHierarchy.getInstance().updateTree();
        camera = window.getCurrentScene().getPanel().getCamera();
        glCanvas.setFocusable(true);
        glCanvas.requestFocus();
        glCanvas.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

                if(e.getKeyCode() == KeyCode.SHIFT.value) {
                    speed = 10;
                    if(forward != 0) {
                        forward = forward > 0 ? speed : -speed;
                    }
                    if(right != 0) {
                        right = right > 0 ? speed : -speed;
                    }
                }

                if(e.getKeyCode() == KeyCode.W.value) {
                    forward = speed;
                }
                if(e.getKeyCode() == KeyCode.A.value) {
                    right = -speed;
                }
                if(e.getKeyCode() == KeyCode.S.value) {
                    forward = -speed;
                }
                if(e.getKeyCode() == KeyCode.D.value) {
                    right = speed;
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyCode.W.value || e.getKeyCode() == KeyCode.S.value) {
                    forward = 0;
                }
                if(e.getKeyCode() == KeyCode.A.value || e.getKeyCode() == KeyCode.D.value) {
                    right = 0;
                }
                if(e.getKeyCode() == KeyCode.SHIFT.value) {
                    speed = 2;
                    if(forward != 0) {
                        forward = forward > 0 ? speed : -speed;
                    }
                    if(right != 0) {
                        right = right > 0 ? speed : -speed;
                    }
                }
            }
        });
        glCanvas.addMouseMotionListener(new MouseMotionListener() {

            private int previousX = 0;
            private int previousY = 0;

            @Override
            public void mouseDragged(MouseEvent e) {
                if(!SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                handleRotation(e.getX(), e.getY(), previousX, previousY);

                previousX = e.getX();
                previousY = e.getY();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }

        });
        new Thread(() -> {
            while(true) {
                handleMovement();
                new WaitForSeconds((double) 1 / 60).here();
            }
        }).start();
    }

    @Override
    public @NotNull JComponent getComponent() {
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return panel;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "GameView";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public VirtualFile getFile() {
        return file;
    }

    @Override
    public void dispose() {
        window.close();
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }

    public BaseWindow getWindow() {
        return this.window;
    }

    public static GameView getInstance() {
        return instance;
    }

    private void handleMovement() {

        TransformSet rotation = camera.getRotation();

        double pitchRad = Math.toRadians(rotation.getX());
        double yawRad = Math.toRadians(rotation.getY());

        // Forward
        double dirX = Math.cos(pitchRad) * Math.sin(yawRad);
        double dirY = Math.sin(pitchRad);
        double dirZ = -Math.cos(pitchRad) * Math.cos(yawRad);

        // Left/right
        double rightX = Math.cos(yawRad);
        double rightZ = Math.sin(yawRad);

        double dx = dirX * forward + rightX * right;
        double dy = dirY * forward + up;
        double dz = dirZ * forward + rightZ * right;

        TransformSet position = new TransformSet(camera.getX() + dx, camera.getY() + dy, camera.getZ() + dz);
        camera.setPosition(position);
    }

    public void handleRotation(int mouseX, int mouseY, int previousX, int previousY) {

        int delta = previousX - mouseX;

        double newRotationY = camera.getRotation().getY() + delta * -1;
        newRotationY = newRotationY % 360;

        delta = previousY - mouseY;

        double newRotationX = camera.getRotation().getX() + delta;
        if(newRotationX > 90) {
            newRotationX = 90;
        } else if(newRotationX < -90) {
            newRotationX = -90;
        }

        TransformSet rotation = new TransformSet(newRotationX, newRotationY, 30);
        camera.setRotation(rotation);
    }

}
