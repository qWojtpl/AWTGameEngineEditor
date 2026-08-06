package pl.AWTGameEngineEditor.settings;

import com.intellij.openapi.application.WriteAction;
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
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class AppSettings implements Configurable {

    private final Project project;
    private JPanel panel;
    private final HashMap<Component, String> defaultValues = new HashMap<>();
    private final HashMap<String, Component> keyComponent = new HashMap<>();

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
            String[] lines = new String(getSettingsVirtualFile().getInputStream().readAllBytes(), StandardCharsets.UTF_8).split("\\r?\\n");
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
                        component = new JBTextField(content);
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
                    defaultValues.put(component, content);
                    keyComponent.put(lineSplit[0], component);
                }
            }
            this.panel = builder.getPanel();
        } catch(IOException ignored) {}
        return panel;
    }

    @Override
    public boolean isModified() {
        for(Component component : defaultValues.keySet()) {
            String defaultValue = defaultValues.get(component);
            if(component instanceof JBCheckBox) {
                if(((JBCheckBox) component).isSelected() != Boolean.parseBoolean(defaultValue)) {
                    return true;
                }
            } else if(component instanceof JBTextField) {
                if(!((JBTextField) component).getText().equals(defaultValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        WriteAction.run(() -> {
            try {
                String[] lines = new String(getSettingsVirtualFile().getInputStream().readAllBytes(), StandardCharsets.UTF_8).split("\\r?\\n");
                OutputStream output = getSettingsVirtualFile().getOutputStream(this);
                for(String line : lines) {
                    String[] split = line.split("=");
                    Component component = keyComponent.getOrDefault(split[0], null);
                    if(component == null) {
                        output.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                        continue;
                    }
                    String value = "";
                    if(component instanceof JBCheckBox) {
                        value = ((JBCheckBox) component).isSelected() + "";
                    } else if(component instanceof JBTextField) {
                        value = ((JBTextField) component).getText();
                    }
                    defaultValues.replace(component, value);
                    output.write((split[0] + "=" + value + "\n").getBytes(StandardCharsets.UTF_8));
                }
                output.flush();
                output.close();
            } catch(IOException e) {
                throw new ConfigurationException(e.getMessage());
            }
        });
    }

    @Override
    public void reset() {
        for(Component component : defaultValues.keySet()) {
            String defaultValue = defaultValues.get(component);
            if(component instanceof JBCheckBox) {
                ((JBCheckBox) component).setSelected(Boolean.parseBoolean(defaultValue));
            } else if(component instanceof JBTextField) {
                ((JBTextField) component).setText(defaultValue);
            }
        }
    }

    private VirtualFile getSettingsVirtualFile() throws IOException {
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        assert projectDir != null;
        VirtualFile appProperties = projectDir.findFileByRelativePath("src/main/resources/app.properties");
        if(appProperties == null) {
            appProperties = projectDir.createChildData(this, "src/main/resources/app.properties");
            Dependencies.getResourceManager().getResource("app.properties");
        }
        return appProperties;
    }

}
