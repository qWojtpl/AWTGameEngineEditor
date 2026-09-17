package pl.AWTGameEngineEditor.center;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.AWTGameEngine.Dependencies;
import pl.AWTGameEngine.engine.Logger;
import pl.AWTGameEngine.engine.PhysXManager;
import pl.AWTGameEngine.engine.WaitForSeconds;
import pl.AWTGameEngine.engine.enums.KeyCode;
import pl.AWTGameEngine.engine.enums.RenderEngine;
import pl.AWTGameEngine.engine.panels.FilamentPanel;
import pl.AWTGameEngine.engine.panels.PanelGL;
import pl.AWTGameEngine.objects.RaycastResult;
import pl.AWTGameEngine.objects.render.Camera;
import pl.AWTGameEngine.objects.transform.Vector3;
import pl.AWTGameEngine.windows.BaseWindow;
import pl.AWTGameEngineEditor.hierarchy.ObjectHierarchy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.util.List;

public class GameView implements FileEditor {

    private static GameView instance;
    private final VirtualFile file;
    private final JPanel panel = new JPanel();
    private BaseWindow window;
    private boolean disposed = false;

    private Camera camera;
    private double forward = 0, right = 0, up = 0;
    private double speed = 2;
    private int previousX = -1;
    private int previousY = -1;

    public GameView(Project project, VirtualFile file) {
        this.file = file;
        instance = this;
        ProgressManager.getInstance().run(new Task.Modal(project, "Loading scene " + file.getName() + "...", false) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                createGameView();
            }

            @Override
            public void onSuccess() {
                ObjectHierarchy.getInstance().updateTree();
            }

        });
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
    public @NotNull String getName() {
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
        window.getPhysicsLoop().start();
        Dependencies.getWindowsManager().close(window);
        disposed = true;
        System.gc();
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

        Vector3 rotation = camera.getRotation();

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

        Vector3 position = new Vector3(camera.getX() + dx, camera.getY() + dy, camera.getZ() + dz);
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

        Vector3 rotation = new Vector3(newRotationX, newRotationY, 30);
        camera.setRotation(rotation);
    }

    private void createGameView() {
        InputStream stream;
        try {
            stream = file.getInputStream();
        } catch(Exception e) {
            stream = null;
        }
        Logger.setLevel(3);
        Logger.setCallerClass(true);
        Logger.setLogFile(false);
        window = Dependencies.getWindowsManager().createNestedEditorWindow(stream, file.getName(), RenderEngine.OPENGL);
        window.getPhysicsLoop().executeNextFrameOperations(); // register all physics objects
        Canvas canvas = null;
        if(window.getCurrentScene().getPanel() instanceof PanelGL panel) {
            canvas = panel.getGlCanvas();
        } else if(window.getCurrentScene().getPanel() instanceof FilamentPanel panel) {
            canvas = panel.getCanvas();
        }
        if(canvas == null) {
            return;
        }
        window.setVisible(false);
        panel.add(canvas);
        camera = window.getCurrentScene().getPanel().getCamera();
        canvas.setFocusable(true);
        canvas.requestFocus();
        registerListeners(canvas);
        new Thread(() -> {
            while(!disposed) {
                handleMovement();
                new WaitForSeconds((double) 1 / 60).here();
            }
        }).start();
    }

    private void registerListeners(Canvas glCanvas) {
        glCanvas.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

                if(e.getKeyCode() == KeyCode.SHIFT.value) {
                    speed = 10;
                    updateSpeed();
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
                if(e.getKeyCode() == KeyCode.Q.value) {
                    up = speed;
                }
                if(e.getKeyCode() == KeyCode.E.value) {
                    up = -speed;
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
                if(e.getKeyCode() == KeyCode.Q.value || e.getKeyCode() == KeyCode.E.value) {
                    up = 0;
                }
                if(e.getKeyCode() == KeyCode.SHIFT.value) {
                    speed = 2;
                    updateSpeed();
                }
            }

            private void updateSpeed() {
                if(forward != 0) {
                    forward = forward > 0 ? speed : -speed;
                }
                if(right != 0) {
                    right = right > 0 ? speed : -speed;
                }
                if(up != 0) {
                    up = up > 0 ? speed : -speed;
                }
            }

        });

        glCanvas.addMouseMotionListener(new MouseMotionListener() {



            @Override
            public void mouseDragged(MouseEvent e) {
                if(!SwingUtilities.isRightMouseButton(e)) {
                    return;
                }

                if(previousX == -1 || previousY == -1) {
                    previousX = e.getX();
                    previousY = e.getY();
                }

                handleRotation(e.getX(), e.getY(), previousX, previousY);

                previousX = e.getX();
                previousY = e.getY();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }

        });
        glCanvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Camera camera = window.getCurrentScene().getPanel().getCamera();

                Vector3 ray = getMouseRayDirection(
                        e.getX(),
                        e.getY(),
                        glCanvas.getWidth(),
                        glCanvas.getHeight(),
                        camera.getRotation(),
                        60
                );

                List<RaycastResult> results = PhysXManager.getInstance().getScene(window.getCurrentScene()).createRaycast(
                        new Vector3(camera.getX(), camera.getY(), camera.getZ()),
                        ray,
                        1000
                );
                if(results.isEmpty()) {
                    System.out.println("EMPTY");
                } else {
                    RaycastResult last = results.get(results.size() - 1);
                    System.out.println(last.getObject().getIdentifier());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                previousX = -1;
                previousY = -1;
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    public static Vector3 getMouseRayDirection(
            float mouseX,
            float mouseY,
            float screenWidth,
            float screenHeight,
            Vector3 rotation,
            float fov
    ) {
        double pitch = Math.toRadians(rotation.getX());
        double yaw = Math.toRadians(rotation.getY());

        Vector3 forward = new Vector3(
                Math.cos(pitch) * Math.sin(yaw),
                Math.sin(pitch),
                -Math.cos(pitch) * Math.cos(yaw)
        ).normalize();

        Vector3 right = new Vector3(
                Math.cos(yaw),
                0,
                Math.sin(yaw)
        ).normalize();

        Vector3 up = new Vector3(
                right.getY() * forward.getZ() - right.getZ() * forward.getY(),
                right.getZ() * forward.getX() - right.getX() * forward.getZ(),
                right.getX() * forward.getY() - right.getY() * forward.getX()
        ).normalize();

        double ndcX = (2.0 * mouseX / screenWidth) - 1.0;
        double ndcY = 1.0 - (2.0 * mouseY / screenHeight);

        double aspect = screenWidth / screenHeight;
        double tanFov = Math.tan(Math.toRadians(fov) * 0.5);

        double cameraX = ndcX * aspect * tanFov;
        double cameraY = ndcY * tanFov;

        return new Vector3(
                forward.getX() + right.getX() * cameraX + up.getX() * cameraY,
                forward.getY() + right.getY() * cameraX + up.getY() * cameraY,
                forward.getZ() + right.getZ() * cameraX + up.getZ() * cameraY
        ).normalize();
    }

}
