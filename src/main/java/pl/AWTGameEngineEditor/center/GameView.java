package pl.AWTGameEngineEditor.center;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.AWTGameEngine.Dependencies;
import pl.AWTGameEngine.engine.enums.RenderEngine;
import pl.AWTGameEngine.engine.panels.PanelGL;
import pl.AWTGameEngine.windows.BaseWindow;
import pl.AWTGameEngine.windows.WindowsManager;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public class GameView implements FileEditor {

    private final VirtualFile file;
    private final JPanel panel = new JPanel();
    private final BaseWindow window;

    public GameView(VirtualFile file) {
        WindowsManager manager = Dependencies.getWindowsManager();
        window = manager.createWindow("scenes/performance/physx_performance.xml", RenderEngine.OPENGL, true);
        panel.add(((PanelGL) window.getCurrentScene().getPanel()).getGljPanel());
        this.file = file;
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
        System.out.println("DISPOSE");
        window.close();
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }

}
