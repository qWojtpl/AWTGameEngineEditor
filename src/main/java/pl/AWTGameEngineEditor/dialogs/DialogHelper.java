package pl.AWTGameEngineEditor.dialogs;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.awt.*;

public class DialogHelper {

    public static JSpinner createFloatSpinner(float value) {
        return correctWidth(new JSpinner(new SpinnerNumberModel(value, null, null, 1.0f)));
    }

    public static JSpinner createDoubleSpinner(double value) {
        return correctWidth(new JSpinner(new SpinnerNumberModel(value, null, null, 1.0)));
    }

    public static JSpinner createIntegerSpinner(int value) {
        return correctWidth(new JSpinner(new SpinnerNumberModel(value, null, null, 1)));
    }

    public static JSpinner createLongSpinner(long value) {
        return correctWidth(new JSpinner(new SpinnerNumberModel(value, null, null, 1L)));
    }

    public static void enableAutoUpdate(JSpinner spinner) {
        JComponent comp = spinner.getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
    }

    public static JSpinner correctWidth(JSpinner spinner) {
        Dimension pref = spinner.getPreferredSize();
        spinner.setPreferredSize(new Dimension(Integer.MAX_VALUE, pref.height));
        return spinner;
    }

}
