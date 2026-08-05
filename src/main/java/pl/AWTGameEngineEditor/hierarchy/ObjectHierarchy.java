package pl.AWTGameEngineEditor.hierarchy;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ObjectHierarchy implements ToolWindowFactory {

    private DefaultMutableTreeNode root;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

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


                } else if(SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popup = new JPopupMenu();
                    if(userObject instanceof GameObject) {
                        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
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

        updateTree();
    }

    private void updateTree() {
        Scene scene = GameView.getInstance().getWindow().getCurrentScene();

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

}
