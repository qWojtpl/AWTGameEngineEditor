package pl.AWTGameEngineEditor.hierarchy;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import org.jetbrains.annotations.NotNull;
import pl.AWTGameEngine.components.base.ObjectComponent;
import pl.AWTGameEngine.engine.Logger;
import pl.AWTGameEngine.objects.GameObject;
import pl.AWTGameEngine.scenes.Scene;
import pl.AWTGameEngineEditor.center.GameView;
import pl.AWTGameEngineEditor.dialogs.AddComponentModel;
import pl.AWTGameEngineEditor.dialogs.ComponentDialog;
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
                    } else if(userObject instanceof ObjectComponent) {
                        new ComponentDialog(project, (ObjectComponent) userObject).show();
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
                            ChooseByNamePopup p = ChooseByNamePopup.createPopup(project, new AddComponentModel(), (PsiElement) null);
                            p.invoke(new ChooseByNamePopupComponent.Callback() {
                                @Override
                                public void elementChosen(Object element) {
                                    Class<? extends ObjectComponent> clazz = ((Class<?>) element).asSubclass(ObjectComponent.class);
                                    try {
                                        ObjectComponent o = clazz.getConstructor(GameObject.class).newInstance((GameObject) userObject);
                                        ((GameObject) userObject).addComponent(o);
                                        updateTree();
                                    } catch (Exception e) {
                                        Logger.exception("Cannot create ObjectComponent", e);
                                    }
                                }
                            }, ModalityState.defaultModalityState(), true);
                        });
                        popup.add(addComponent);
                        // Remove object
                        JMenuItem removeObject = new JMenuItem("Remove object", AllIcons.General.Remove);
                        removeObject.addActionListener(al -> {
                            GameView.getInstance().getWindow().getCurrentScene().removeGameObject((GameObject) userObject);
                            ObjectHierarchy.getInstance().updateTree();
                        });
                        popup.add(removeObject);
                    } else if(userObject instanceof ObjectComponent) {
                        // Edit component
                        JMenuItem editComponent = new JMenuItem("Edit component properties", AllIcons.General.Modified);
                        editComponent.addActionListener(al -> {
                            new ComponentDialog(project, (ObjectComponent) userObject).show();
                        });
                        popup.add(editComponent);
                        // Remove component
                        JMenuItem removeComponent = new JMenuItem("Remove component", AllIcons.General.Remove);
                        removeComponent.addActionListener(al -> {
                            ((ObjectComponent) userObject).getObject().removeComponent((ObjectComponent) userObject);
                            ObjectHierarchy.getInstance().updateTree();
                        });
                        popup.add(removeComponent);
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
