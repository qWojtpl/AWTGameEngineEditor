package pl.AWTGameEngineEditor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class Factory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

/*
        Logger.setLevel(3);
        Logger.setCallerClass(true);
        Logger.setLogFile(false);

        WindowsManager manager = Dependencies.getWindowsManager();
        BaseWindow window = manager.createWindow("scenes/topdown.xml", RenderEngine.DEFAULT, true);
*/

        JPanel panel = new JPanel();
//        panel.add(((DefaultPanel) window.getPanels().get(0)));
//        panel.add(((PanelGL) window.getPanels().get(0)).getGljPanel());

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(
                panel,
                "",
                false
        );

        toolWindow.getContentManager().addContent(content);
    }

}
