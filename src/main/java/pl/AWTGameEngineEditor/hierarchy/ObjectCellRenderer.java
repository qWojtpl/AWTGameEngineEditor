package pl.AWTGameEngineEditor.hierarchy;

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import pl.AWTGameEngine.components.base.ObjectComponent;
import pl.AWTGameEngine.objects.GameObject;
import pl.AWTGameEngine.scenes.Scene;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class ObjectCellRenderer extends ColoredTreeCellRenderer {

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if(!(value instanceof DefaultMutableTreeNode node)) {
            return;
        }
        Object userObject = node.getUserObject();
        if(userObject instanceof Scene) {
            setIcon(AllIcons.Nodes.Bookmark);
            append(((Scene) userObject).getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
        } else if(userObject instanceof GameObject) {
            setIcon(AllIcons.Toolwindows.Documentation);
            append(((GameObject) userObject).getIdentifier());
        } else if(userObject instanceof ObjectComponent) {
            setIcon(AllIcons.Nodes.Module);
            append(((ObjectComponent) userObject).getComponentName());
        }
    }

}
