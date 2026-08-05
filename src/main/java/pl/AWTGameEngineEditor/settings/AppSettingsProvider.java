package pl.AWTGameEngineEditor.settings;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import pl.AWTGameEngineEditor.util.DummyEditor;

import java.util.concurrent.atomic.AtomicBoolean;

public class AppSettingsProvider implements FileEditorProvider, DumbAware {

    private final AtomicBoolean opened = new AtomicBoolean(false);

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return "app.properties".equals(file.getName());
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        // Prevent double-click
        if(opened.get()) {
            opened.set(false);
            return new DummyEditor(project, file, () -> {});
        }
        return new DummyEditor(project, file, () -> {
            opened.set(true);
            ShowSettingsUtil.getInstance().showSettingsDialog(project, AppSettings.class);
        });
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return "AWTGameEngineEditor.AppSettings";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

}
