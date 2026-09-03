package pl.AWTGameEngineEditor.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;
import pl.AWTGameEngine.objects.GameObject;
import pl.AWTGameEngine.objects.transform.Vector3;
import pl.AWTGameEngineEditor.preview.Preview;
import pl.AWTGameEngineEditor.preview.PreviewManager;

import javax.swing.*;
import java.util.HashMap;

public class ObjectPropertiesDialog extends DialogWrapper {

    private final GameObject gameObject;
    private final HashMap<String, JSpinner> spinners = new HashMap<>();
    private final Preview preview = new Preview();

    private JBCheckBox quaternionCheckBox;

    public ObjectPropertiesDialog(@Nullable Project project, GameObject gameObject) {
        super(project, true, true);
        this.gameObject = gameObject;
        setTitle("Edit Object Properties");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormBuilder builder = new FormBuilder();
        setupForm(builder);
        PreviewManager.create(builder, preview, PreviewManager.standardObjectPreview(gameObject));
        for(String name : spinners.keySet()) {
            DialogHelper.enableAutoUpdate(spinners.get(name));
            addSpinnerListener(name, spinners.get(name));
        }
        return builder.getPanel();
    }

    private void setupForm(FormBuilder builder) {
        builder.addLabeledComponent("Identifier", new JBTextField(gameObject.getIdentifier()));
        builder.addSeparator().addComponent(new JBLabel("Position"));
        builder.addLabeledComponent("X", createSpinner("posX", gameObject.getPosition().getX()));
        builder.addLabeledComponent("Y", createSpinner("posY", gameObject.getPosition().getY()));
        builder.addLabeledComponent("Z", createSpinner("posZ", gameObject.getPosition().getZ()));
        builder.addSeparator().addComponent(new JBLabel("Size"));
        builder.addLabeledComponent("X", createSpinner("sizX", gameObject.getSize().getX()));
        builder.addLabeledComponent("Y", createSpinner("sizY", gameObject.getSize().getY()));
        builder.addLabeledComponent("Z", createSpinner("sizZ", gameObject.getSize().getZ()));
        builder.addSeparator().addComponent(new JBLabel("Rotation"));
        builder.addLabeledComponent("X", createSpinner("rotX", gameObject.getRotation().getX()));
        builder.addLabeledComponent("Y", createSpinner("rotY", gameObject.getRotation().getY()));
        builder.addLabeledComponent("Z", createSpinner("rotZ", gameObject.getRotation().getZ()));
        LabeledComponent<JSpinner> wRotation = new LabeledComponent<>();
        wRotation.setText("W");
        wRotation.setComponent(createSpinner("rotW", gameObject.getQuaternionRotation().getW()));
        builder.addComponent(wRotation);
        wRotation.setVisible(false);
        quaternionCheckBox = new JBCheckBox("Quaternion rotation?", false);
        builder.addComponent(quaternionCheckBox);
        quaternionCheckBox.addChangeListener(e -> {
            wRotation.setVisible(quaternionCheckBox.isSelected());
            if(quaternionCheckBox.isSelected()) {
                spinners.get("rotX").setValue(gameObject.getQuaternionRotation().getX());
                spinners.get("rotY").setValue(gameObject.getQuaternionRotation().getY());
                spinners.get("rotZ").setValue(gameObject.getQuaternionRotation().getZ());
                spinners.get("rotW").setValue(gameObject.getQuaternionRotation().getW());
            } else {
                spinners.get("rotX").setValue(gameObject.getRotation().getX());
                spinners.get("rotY").setValue(gameObject.getRotation().getY());
                spinners.get("rotZ").setValue(gameObject.getRotation().getZ());
            }
        });
    }

    private void addSpinnerListener(String spinnerName, JSpinner spinner) {
        spinner.addChangeListener(e -> {
            if(spinnerName.startsWith("pos")) {
                gameObject.getPosition().set(
                        (double) spinners.get("posX").getValue(),
                        (double) spinners.get("posY").getValue(),
                        (double) spinners.get("posZ").getValue()
                );
            } else if(spinnerName.startsWith("siz")) {
                gameObject.getSize().set(
                        (double) spinners.get("sizX").getValue(),
                        (double) spinners.get("sizY").getValue(),
                        (double) spinners.get("sizZ").getValue()
                );
            } else if(spinnerName.startsWith("rot")) {
                if(quaternionCheckBox.isSelected()) {
                    gameObject.getQuaternionRotation().setX((double) spinners.get("rotX").getValue());
                    gameObject.getQuaternionRotation().setY((double) spinners.get("rotY").getValue());
                    gameObject.getQuaternionRotation().setZ((double) spinners.get("rotZ").getValue());
                    gameObject.getQuaternionRotation().setW((double) spinners.get("rotW").getValue());
                } else {
                    gameObject.setRotation(new Vector3(
                            (double) spinners.get("rotX").getValue(),
                            (double) spinners.get("rotY").getValue(),
                            (double) spinners.get("rotZ").getValue()
                    ));
                }
            }
            PreviewManager.update(preview, PreviewManager.standardObjectPreview(gameObject));
        });
    }

    private JSpinner createSpinner(String name, double value) {
        JSpinner spinner = DialogHelper.createDoubleSpinner(value);
        spinners.put(name, spinner);
        return spinner;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

}
