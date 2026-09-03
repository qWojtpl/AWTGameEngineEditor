package pl.AWTGameEngineEditor.dialogs;

import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.openapi.util.NlsContexts;
import com.jetbrains.rd.util.reflection.ReflectionScannerKt;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.AWTGameEngine.components.base.ObjectComponent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddComponentModel implements ChooseByNameModel {

    private final List<Class<?>> classList = new ArrayList<>();

    public AddComponentModel() {
        Sequence<Class<?>> classSequence = ReflectionScannerKt.scanForClasses(getClass().getClassLoader(), "pl.AWTGameEngine.components");
        Iterator<Class<?>> iterator = classSequence.iterator();

        while(iterator.hasNext()) {
            Class<?> clazz = iterator.next();
            if(ObjectComponent.class.isAssignableFrom(clazz)) {
                classList.add(clazz);
            }
        }
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getPromptText() {
        return "Select component";
    }

    @Override
    public @NotNull @NlsContexts.Label String getNotInMessage() {
        return "";
    }

    @Override
    public @NotNull @NlsContexts.Label String getNotFoundMessage() {
        return "";
    }

    @Override
    public @Nullable @NlsContexts.Label String getCheckBoxName() {
        return null;
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return false;
    }

    @Override
    public void saveInitialCheckBoxState(boolean state) {}

    @Override
    public @NotNull ListCellRenderer<?> getListCellRenderer() {
        return new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Class<?> clazz) {
                    setText(clazz.getCanonicalName());
                }

                return this;
            }

        };
    }

    @Override
    public String @NotNull @Nls [] getNames(boolean checkBoxState) {
        return classList.stream()
                .map(Class::getCanonicalName)
                .toArray(String[]::new);
    }

    @Override
    public Object @NotNull [] getElementsByName(@NotNull String name, boolean checkBoxState, @NotNull String pattern) {
        return classList.stream()
                .filter(clazz -> clazz.getCanonicalName().equals(name))
                .toArray();
    }

    @Override
    public @Nullable String getElementName(@NotNull Object element) {
        if(element instanceof Class<?>) {
            return ((Class<?>) element).getCanonicalName();
        }
        return null;
    }

    @Override
    public String @NotNull [] getSeparators() {
        return new String[0];
    }

    @Override
    public @Nullable String getFullName(@NotNull Object element) {
        if(element instanceof Class<?>) {
            return ((Class<?>) element).getCanonicalName();
        }
        return null;
    }

    @Override
    public @Nullable @NonNls String getHelpId() {
        return "";
    }

    @Override
    public boolean willOpenEditor() {
        return false;
    }

    @Override
    public boolean useMiddleMatching() {
        return true;
    }

}
