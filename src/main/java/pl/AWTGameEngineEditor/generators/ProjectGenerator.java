package pl.AWTGameEngineEditor.generators;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ProjectGenerator extends ModuleBuilder {

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
        return "AWT Game Engine";
    }

    @Override
    public @Nullable @NonNls String getBuilderId() {
        return "AWTGameEngine";
    }

    @Override
    public ModuleType<?> getModuleType() {
        return EngineModule.getInstance();
    }

    @Override
    public Icon getNodeIcon() {
        return null;
    }


}
