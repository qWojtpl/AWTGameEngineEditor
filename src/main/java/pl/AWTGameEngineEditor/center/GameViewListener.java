package pl.AWTGameEngineEditor.center;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class GameViewListener implements FileEditorManagerListener {

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        VirtualFile oldFile = event.getOldFile();
        VirtualFile newFile = event.getNewFile();

        if(oldFile == null || newFile == null) {
            return;
        }

        if(!("xml".equals(oldFile.getExtension()))) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            FileEditorManager manager = FileEditorManager.getInstance(event.getManager().getProject());

            if(manager.isFileOpen(oldFile)) {
                manager.closeFile(oldFile);
            }
        });
    }

}
