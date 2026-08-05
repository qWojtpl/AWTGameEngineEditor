package pl.AWTGameEngineEditor.hierarchy;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import org.jetbrains.annotations.NotNull;
import pl.AWTGameEngine.components.base.ObjectComponent;
import pl.AWTGameEngine.objects.GameObject;
import pl.AWTGameEngine.scenes.Scene;
import pl.AWTGameEngineEditor.center.GameView;
import pl.AWTGameEngineEditor.dialogs.ObjectPropertiesDialog;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ObjectHierarchy implements ToolWindowFactory {

    private static ObjectHierarchy instance;
    private DefaultMutableTreeNode root;
    private ToolWindow toolWindow;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        instance = this;
        this.toolWindow = toolWindow;

        root = new DefaultMutableTreeNode();

        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        SimpleTree tree = new SimpleTree(treeModel);
        tree.setRootVisible(true);
        tree.setCellRenderer(new ObjectCellRenderer());

        JBScrollPane scrollPane = new JBScrollPane(tree);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(scrollPane, "", false);
        toolWindow.getContentManager().addContent(content);

        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if(path == null) {
                    return;
                }
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObject = node.getUserObject();

                if(e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    if(userObject instanceof GameObject) {
                        new ObjectPropertiesDialog(project, (GameObject) userObject).show();
                    }

                } else if(SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popup = new JPopupMenu();
                    if(userObject instanceof GameObject) {
                        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        // Edit
                        JMenuItem editObject = new JMenuItem("Edit object properties", AllIcons.General.Modified);
                        editObject.addActionListener(al -> {
                            new ObjectPropertiesDialog(project, (GameObject) userObject).show();
                        });
                        popup.add(editObject);
                        // Add component
                        JMenuItem addComponent = new JMenuItem("Add component", AllIcons.General.Add);
                        addComponent.addActionListener(al -> {

                        });
                        popup.add(addComponent);
                        // Remove object
                        JMenuItem removeObject = new JMenuItem("Remove object", AllIcons.General.Remove);
                        removeObject.addActionListener(al -> {

                        });
                        popup.add(removeObject);
                    }
                    popup.show(tree, e.getX(), e.getY());
                }
            }

        });

        if(GameView.getInstance() != null) {
            updateTree();
        }
    }

    public void updateTree() {
        Scene scene = GameView.getInstance().getWindow().getCurrentScene();
        toolWindow.setAnchor(ToolWindowAnchor.RIGHT, () -> {});

        root.setUserObject(scene);

        for(GameObject object : scene.getGameObjects()) {
            DefaultMutableTreeNode objectNode = new DefaultMutableTreeNode();
            objectNode.setUserObject(object);
            root.add(objectNode);
            for(ObjectComponent component : object.getComponents()) {
                DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode();
                componentNode.setUserObject(component);
                objectNode.add(componentNode);
            }
        }
    }

    public static ObjectHierarchy getInstance() {
        return instance;
    }

}
