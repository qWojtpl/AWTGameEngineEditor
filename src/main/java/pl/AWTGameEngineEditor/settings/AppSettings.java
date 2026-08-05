package pl.AWTGameEngineEditor.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColorPicker;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;
import pl.AWTGameEngine.Dependencies;
import pl.AWTGameEngine.objects.ColorObject;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AppSettings implements Configurable {

    private final Project project;
    private JPanel panel;

    public AppSettings(Project project) {
        this.project = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "AWTGameEngine";
    }

    @Override
    public @Nullable JComponent createComponent() {
        try {
            VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
            assert projectDir != null;
            VirtualFile appProperties = projectDir.findFileByRelativePath("src/main/resources/app.properties");
            if(appProperties == null) {
                appProperties = projectDir.createChildData(this, "src/main/resources/app.properties");
                Dependencies.getResourceManager().getResource("app.properties");
            }
            String[] lines = new String(appProperties.getInputStream().readAllBytes(), StandardCharsets.UTF_8).split("\\n");
            FormBuilder builder = FormBuilder.createFormBuilder();
            boolean separator = false;
            for(String line : lines) {
                if(line.trim().isEmpty()) {
                    continue;
                }
                if(line.startsWith("#")) {
                    if(!separator) {
                        builder.addSeparator();
                        separator = true;
                    }
                    builder.addTooltip(line.replace("#", ""));
                } else {
                    if(separator) {
                        separator = false;
                    }
                    String[] lineSplit = line.split("=");
                    String title = lineSplit[0].substring(0, 1).toUpperCase() + lineSplit[0].substring(1);
                    String content = lineSplit[1].trim();
                    JComponent component;
                    if(content.equalsIgnoreCase("true") || content.equalsIgnoreCase("false")) {
                        component = new JBCheckBox(null, Boolean.parseBoolean(content));
                    } else if(content.toLowerCase().startsWith("rgb")) {
                        component = new JButton(content);
                        component.addMouseListener(new MouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                ColorPicker.showDialog(component, title, new ColorObject(content).getColor(), true, null, true);
                            }

                            @Override
                            public void mousePressed(MouseEvent e) {

                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {

                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {

                            }

                            @Override
                            public void mouseExited(MouseEvent e) {

                            }
                        });
                    } else {
                        component = new JBTextField(content);
                    }
                    builder.addLabeledComponent(title, component);
                }
            }
            this.panel = builder.getPanel();
        } catch(IOException ignored) {}
        return panel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

}
