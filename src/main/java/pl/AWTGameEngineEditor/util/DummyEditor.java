package pl.AWTGameEngineEditor.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public class DummyEditor extends UserDataHolderBase implements FileEditor {

    private final VirtualFile file;
    private final JPanel dummyPanel = new JPanel();

    public DummyEditor(Project project, VirtualFile file, Runnable operation) {
        this.file = file;
        ApplicationManager.getApplication().invokeLater(() -> {
            FileEditorManager.getInstance(project).closeFile(file);
            operation.run();
        });
    }

    @Override
    public @NotNull JComponent getComponent() {
        return dummyPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return file.getName();
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

    }
}
