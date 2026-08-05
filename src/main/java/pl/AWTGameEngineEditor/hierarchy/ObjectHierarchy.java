package pl.AWTGameEngineEditor.hierarchy;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import pl.AWTGameEngine.components.base.ObjectComponent;
import pl.AWTGameEngine.engine.WaitForSeconds;
import pl.AWTGameEngine.objects.GameObject;
import pl.AWTGameEngine.scenes.Scene;
import pl.AWTGameEngineEditor.center.GameView;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ObjectHierarchy implements ToolWindowFactory {

    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private SimpleTree tree;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        Scene scene = GameView.getInstance().getWindow().getCurrentScene();

        root = new DefaultMutableTreeNode(scene.getName());

        treeModel = new DefaultTreeModel(root);
        tree = new SimpleTree(treeModel);
        tree.setRootVisible(true);

        JBScrollPane scrollPane = new JBScrollPane(tree);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(scrollPane, "", false);
        toolWindow.getContentManager().addContent(content);
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
            ApplicationManager.getApplication().invokeLater(this::updateTree);
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void updateTree() {
        Scene scene = GameView.getInstance().getWindow().getCurrentScene();

        Set<String> expandedNodeNames = new HashSet<>();
        Enumeration<TreePath> expandedPaths = tree.getExpandedDescendants(new TreePath(root));
        if (expandedPaths != null) {
            while (expandedPaths.hasMoreElements()) {
                TreePath path = expandedPaths.nextElement();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() != null) {
                    expandedNodeNames.add(node.getUserObject().toString());
                }
            }
        }

        root.setUserObject(scene.getName());

        root.removeAllChildren();

        for(GameObject object : scene.getGameObjects()) {
            DefaultMutableTreeNode objectNode = new DefaultMutableTreeNode(object.getIdentifier());
            root.add(objectNode);
            for(ObjectComponent component : object.getComponents()) {
                DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(component.getClass().getCanonicalName());
                objectNode.add(componentNode);
            }
        }
        treeModel.reload();
        restoreExpansionState(tree, root, expandedNodeNames);
    }

    private void restoreExpansionState(SimpleTree tree, DefaultMutableTreeNode node, Set<String> expandedNames) {
        if (node.getUserObject() != null && expandedNames.contains(node.getUserObject().toString())) {
            tree.expandPath(new TreePath(node.getPath()));
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            restoreExpansionState(tree, (DefaultMutableTreeNode) node.getChildAt(i), expandedNames);
        }
    }

}
