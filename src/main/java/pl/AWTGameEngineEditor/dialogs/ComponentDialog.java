package pl.AWTGameEngineEditor.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.jogamp.opengl.awt.GLCanvas;
import org.jetbrains.annotations.Nullable;
import pl.AWTGameEngine.Dependencies;
import pl.AWTGameEngine.annotations.methods.FromXML;
import pl.AWTGameEngine.annotations.methods.SaveState;
import pl.AWTGameEngine.components.ParticleEmitter;
import pl.AWTGameEngine.components.base.ObjectComponent;
import pl.AWTGameEngine.engine.Logger;
import pl.AWTGameEngine.engine.deserializers.XMLDeserializer;
import pl.AWTGameEngine.engine.enums.RenderEngine;
import pl.AWTGameEngine.engine.panels.PanelGL;
import pl.AWTGameEngine.objects.GameObject;
import pl.AWTGameEngine.objects.transform.TransformSet;
import pl.AWTGameEngine.windows.Window;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ComponentDialog extends DialogWrapper {

    private final ObjectComponent component;
    private Window particleEmitterWindow;
    private final HashMap<String, Component> fields = new HashMap<>();
    private final HashMap<Component, Class<?>> fieldTypes = new HashMap<>();

    public ComponentDialog(@Nullable Project project, ObjectComponent component) {
        super(project);
        this.component = component;
        setTitle("Edit " + component.getComponentName() + " in " + component.getObject().getIdentifier());
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormBuilder builder = new FormBuilder();
        for(Method method : component.getClass().getMethods()) {
            if(!method.isAnnotationPresent(SaveState.class)) {
                continue;
            }
            SaveState saveState = method.getAnnotation(SaveState.class);
            try {
                Object value = method.invoke(component);
                if(value instanceof Float) {
                    JSpinner spinner = DialogHelper.createFloatSpinner((float) value);
                    builder.addLabeledComponent(saveState.name(), spinner);
                    fields.put(saveState.name(), spinner);
                    fieldTypes.put(spinner, Float.class);
                } else if(value instanceof Double) {
                    JSpinner spinner = DialogHelper.createDoubleSpinner((double) value);
                    builder.addLabeledComponent(saveState.name(), spinner);
                    fields.put(saveState.name(), spinner);
                    fieldTypes.put(spinner, Double.class);
                } else if(value instanceof Integer) {
                    JSpinner spinner = DialogHelper.createIntegerSpinner((int) value);
                    builder.addLabeledComponent(saveState.name(), spinner);
                    fields.put(saveState.name(), spinner);
                    fieldTypes.put(spinner, Integer.class);
                } else if(value instanceof Long) {
                    JSpinner spinner = DialogHelper.createLongSpinner((long) value);
                    builder.addLabeledComponent(saveState.name(), spinner);
                    fields.put(saveState.name(), spinner);
                    fieldTypes.put(spinner, Long.class);
                } else if(value instanceof TransformSet transformSet) {
                    builder.addSeparator();
                    builder.addComponent(new JBLabel(saveState.name()));
                    JSpinner x = DialogHelper.createDoubleSpinner(transformSet.getX());
                    JSpinner y = DialogHelper.createDoubleSpinner(transformSet.getY());
                    JSpinner z = DialogHelper.createDoubleSpinner(transformSet.getZ());
                    builder.addLabeledComponent("X", x);
                    builder.addLabeledComponent("Y", y);
                    builder.addLabeledComponent("Z", z);
                    fields.put(saveState.name() + "$x", x);
                    fields.put(saveState.name() + "$y", y);
                    fields.put(saveState.name() + "$z", z);
                    builder.addSeparator();
                } else {
                    JBTextField field = new JBTextField(value.toString());
                    fields.put(saveState.name(), field);
                    builder.addLabeledComponent(saveState.name(), field);
                }
            } catch(Exception e) {
                Logger.exception("Can't get variable from " + method.getName(), e);
            }
        }
        for(String name : fields.keySet()) {
            if(fields.get(name) instanceof JSpinner spinner) {
                DialogHelper.enableAutoUpdate(spinner);
                spinner.addChangeListener(e -> {
                    if(name.contains("$")) {
                        return;
                    }
                    String passValue = "";
                    Class<?> fieldType = fieldTypes.get(fields.get(name));
                    if(fieldType.equals(Float.class)) {
                        passValue = Float.parseFloat(spinner.getValue() + "") + "";
                    }
                    if(fieldType.equals(Double.class)) {
                        passValue = Double.parseDouble(spinner.getValue() + "") + "";
                    }
                    if(fieldType.equals(Integer.class)) {
                        passValue = Integer.parseInt(spinner.getValue() + "") + "";
                    }
                    if(fieldType.equals(Long.class)) {
                        passValue = Long.parseLong(spinner.getValue() + "") + "";
                    }
                    XMLDeserializer.getInstance().handleSetMethod(
                            component,
                            "set" + name.substring(0,1).toUpperCase() + name.substring(1),
                            passValue
                    );
                    if(component instanceof ParticleEmitter) {
                        updateEmitter((ParticleEmitter) component);
                    }
                });
            } else if(fields.get(name) instanceof JBTextField field) {
                field.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        changedUpdate(e);
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        changedUpdate(e);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        String passValue = field.getText();
                        XMLDeserializer.getInstance().handleSetMethod(
                                component,
                                "set" + name.substring(0,1).toUpperCase() + name.substring(1),
                                passValue
                        );
                        if(component instanceof ParticleEmitter) {
                            updateEmitter((ParticleEmitter) component);
                        }
                    }
                });
            }

        }
        if(component instanceof ParticleEmitter) {
            handleParticleEmitter(builder, (ParticleEmitter) component);
        }
        return builder.getPanel();
    }

    @Override
    public void doCancelAction() {
        disposeEmitter();
        super.doCancelAction();
    }

    @Override
    protected void doOKAction() {
        disposeEmitter();
        super.doOKAction();
    }

    private void disposeEmitter() {
        if(particleEmitterWindow == null) {
            return;
        }
        particleEmitterWindow.getUpdateLoop().kill();
        particleEmitterWindow.getRenderLoop().kill();
        particleEmitterWindow.unloadScenes();
        Dependencies.getWindowsManager().removeWindow(particleEmitterWindow);
        System.gc();
        particleEmitterWindow = null;
    }

    private void handleParticleEmitter(FormBuilder builder, ParticleEmitter e) {
        particleEmitterWindow = (Window) Dependencies.getWindowsManager().createNestedEditorWindow(
                Dependencies.getResourceManager().getResourceAsStream("editorScenes/particleEditor.xml"),
                "editorScenes/particleEditor.xml", RenderEngine.OPENGL);
        particleEmitterWindow.getRenderLoop().setTargetFps(60);
        particleEmitterWindow.getUpdateLoop().setTargetFps(60);
        particleEmitterWindow.getUpdateLoop().start();
        JPanel panel = new JPanel();
        GLCanvas canvas = ((PanelGL) particleEmitterWindow.getCurrentScene().getPanel()).getGlCanvas();
        canvas.setSize(320, 180);
        panel.add(canvas);
        updateEmitter(e);
        builder.addComponent(panel);
    }

    private void updateEmitter(ParticleEmitter e) {
        GameObject object = particleEmitterWindow.getCurrentScene().getGameObjectByName("emitter");
        ParticleEmitter emitter = (ParticleEmitter) object.getComponentByClass(ParticleEmitter.class);

        emitter.setFadeOutStart(e.getFadeOutStart());
        emitter.setParticleSize(e.getParticleSize());
        emitter.setLooped(e.isLooped());
        emitter.setSprite(e.getSprite());
        emitter.setTtl(e.getTtl());
        emitter.setIterationsPerSecond(e.getIterationsPerSecond());
        emitter.setIterationStep(e.getIterationStep());
        emitter.setVectors(e.getVectors());
    }

}
