package pl.AWTGameEngineEditor.generators;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class EngineModule extends ModuleType<ModuleBuilder> {

    private static final EngineModule module = new EngineModule();

    EngineModule() {
        super("AWTGameEngine");
    }

    public static EngineModule getInstance() {
        return module;
    }

    @Override
    public @NotNull ModuleBuilder createModuleBuilder() {
        return new ProjectGenerator();
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getName() {
        return "AWTGameEngine";
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getDescription() {
        return "";
    }

    @Override
    public @NotNull Icon getNodeIcon(boolean isOpened) {
        return AllIcons.Nodes.Project;
    }

    @Override
    public ModuleWizardStep @NotNull [] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModuleBuilder moduleBuilder, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{new ProjectSettings()};
    }
}
