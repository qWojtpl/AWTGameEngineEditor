package pl.AWTGameEngineEditor.dialogs;

import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;
import pl.AWTGameEngine.Dependencies;
import pl.AWTGameEngine.annotations.methods.SaveState;
import pl.AWTGameEngine.components.base.ObjectComponent;
import pl.AWTGameEngine.engine.Logger;
import pl.AWTGameEngine.engine.deserializers.XMLDeserializer;
import pl.AWTGameEngine.engine.helpers.TextUtils;
import pl.AWTGameEngine.objects.GameObject;
import pl.AWTGameEngine.objects.render.Sprite;
import pl.AWTGameEngine.objects.render.shaders.Shader;
import pl.AWTGameEngine.objects.transform.Vector3;
import pl.AWTGameEngineEditor.preview.Preview;
import pl.AWTGameEngineEditor.preview.PreviewManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ComponentDialog extends DialogWrapper {

    private final Project project;
    private final ObjectComponent component;
    private final HashMap<String, Component> fields = new HashMap<>();
    private final HashMap<Component, Class<?>> fieldTypes = new HashMap<>();
    private final Preview preview = new Preview();

    public ComponentDialog(@Nullable Project project, ObjectComponent component) {
        super(project);
        this.project = project;
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
                    builder.addLabeledComponent(TextUtils.toSentenceCase(saveState.name()), spinner);
                    fields.put(saveState.name(), spinner);
                    fieldTypes.put(spinner, Float.class);
                } else if(value instanceof Double) {
                    JSpinner spinner = DialogHelper.createDoubleSpinner((double) value);
                    builder.addLabeledComponent(TextUtils.toSentenceCase(saveState.name()), spinner);
                    fields.put(saveState.name(), spinner);
                    fieldTypes.put(spinner, Double.class);
                } else if(value instanceof Integer) {
                    JSpinner spinner = DialogHelper.createIntegerSpinner((int) value);
                    builder.addLabeledComponent(TextUtils.toSentenceCase(saveState.name()), spinner);
                    fields.put(saveState.name(), spinner);
                    fieldTypes.put(spinner, Integer.class);
                } else if(value instanceof Long) {
                    JSpinner spinner = DialogHelper.createLongSpinner((long) value);
                    builder.addLabeledComponent(TextUtils.toSentenceCase(saveState.name()), spinner);
                    fields.put(saveState.name(), spinner);
                    fieldTypes.put(spinner, Long.class);
                } else if(value instanceof Vector3 vector3) {
                    builder.addSeparator();
                    builder.addComponent(new JBLabel(TextUtils.toSentenceCase(saveState.name())));
                    JSpinner x = DialogHelper.createDoubleSpinner(vector3.getX());
                    JSpinner y = DialogHelper.createDoubleSpinner(vector3.getY());
                    JSpinner z = DialogHelper.createDoubleSpinner(vector3.getZ());
                    builder.addLabeledComponent("X", x);
                    builder.addLabeledComponent("Y", y);
                    builder.addLabeledComponent("Z", z);
                    fields.put(saveState.name() + "$x", x);
                    fields.put(saveState.name() + "$y", y);
                    fields.put(saveState.name() + "$z", z);
                    builder.addSeparator();
                } else if(value instanceof Sprite sprite) {
                    JButton button = new JButton(sprite.getImagePath(), new ImageIcon(sprite.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
                    builder.addLabeledComponent(TextUtils.toSentenceCase(saveState.name()), button);
                } else if(value instanceof Shader shader) {
                    JButton button = new JButton(shader.getClass().getSimpleName());
                    button.addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ChooseByNamePopup p = ChooseByNamePopup.createPopup(project, new AddClassModel("Select shader", Shader.class, "pl.AWTGameEngine.objects.render.shaders"), (PsiElement) null);
                            p.invoke(new ChooseByNamePopupComponent.Callback() {
                                @Override
                                public void elementChosen(Object element) {
                                    button.setText(((Class<?>) element).getSimpleName());
                                    XMLDeserializer.getInstance().handleSetMethod(
                                            component,
                                            "set" + saveState.name().substring(0,1).toUpperCase() + saveState.name().substring(1),
                                            ((Class<?>) element).getCanonicalName()
                                    );
                                    PreviewManager.update(preview, component);
                                }
                            }, ModalityState.defaultModalityState(), false);
                        }
                    });
                    builder.addLabeledComponent(TextUtils.toSentenceCase(saveState.name()), button);
                } else {
                    JBTextField field = new JBTextField(value.toString());
                    fields.put(saveState.name(), field);
                    builder.addLabeledComponent(TextUtils.toSentenceCase(saveState.name()), field);
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
                    PreviewManager.update(preview, component);
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
                        PreviewManager.update(preview, component);
                    }
                });
            }

        }

        PreviewManager.create(builder, preview, component);
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
        if(preview == null) {
            return;
        }
        preview.disposePreview();
    }

}
