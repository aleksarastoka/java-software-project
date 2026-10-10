package raf.graffito.dsw.controller.actions.serializerActions;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.decorator.ColorDecorator;
import raf.graffito.dsw.core.model.decorator.Decorators;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite;
import raf.graffito.dsw.core.model.repository.implementation.Presentation;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.serializer.ProjectFileRegistry;
import raf.graffito.dsw.core.serializer.ProjectJsonSerializer;
import raf.graffito.dsw.gui.swing.color.ColorTags;
import raf.graffito.dsw.gui.swing.tree.GraffTree;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;

public class LoadProjectAction extends AbstractGraffAction {

    private final ProjectJsonSerializer serializer;

    public LoadProjectAction() {
        putValue(NAME, "Load Project");
        putValue(SHORT_DESCRIPTION, "Load a saved project into the Workspace");
        putValue(SMALL_ICON, loadIcon("/images/loadProjectIcon.png"));
        serializer = ApplicationFramework.getInstance().getProjectJsonSerializer();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load Project");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileNameExtensionFilter("Graffito Project (*.json)", "json"));

        int choice = fc.showOpenDialog(MainFrame.getInstance());
        if (choice != JFileChooser.APPROVE_OPTION) return;

        File selected = fc.getSelectedFile();
        if (selected == null) return;

        try {
            Path path = selected.toPath();

            // loads into Workspace (your serializer/mapper should do that)
            Project loaded = serializer.load(path);

            var tree = MainFrame.getInstance().getGraffTree();
            GraffTreeItem root = tree.getRoot();

            // avoid duplicates if it already exists
            GraffTreeItem existing = tree.findGraffTreeItem(root, loaded);
            if (existing != null) {
                JOptionPane.showMessageDialog(MainFrame.getInstance(),
                        "That project is already in the Workspace tree.",
                        "Load Project",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 1) insert the project node
            GraffTreeItem projectItem = new GraffTreeItem(loaded);
            tree.createNode(root, projectItem);

            // 2) insert its subtree (presentations + slides + slide elements)
            buildSubtree(tree, projectItem, loaded);

            ProjectFileRegistry.setPath(loaded, path);

            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    "Project loaded into Workspace:\n" + loaded.getName(),
                    "Load Project",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    "Failed to load project:\n" + ex.getMessage(),
                    "Load Project",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildSubtree(GraffTree tree, GraffTreeItem uiParent, GraffNode modelParent) {
        GraffNode coreParent = Decorators.unwrap(modelParent);

        if (!(coreParent instanceof GraffNodeComposite composite)) return;
        if (composite.getChildren() == null) return;

        for (GraffNode child : composite.getChildren()) {
            GraffNode coreChild = Decorators.unwrap(child);

            // ✅ Now we also show SlideElements as children of Slides
            boolean show =
                    coreChild instanceof Project ||
                            coreChild instanceof Presentation ||
                            coreChild instanceof Slide ||
                            coreChild instanceof SlideElement;

            if (!show) continue;

            // ✅ Presentations in the tree should be decorated so renderer draws the color dot
            GraffNode treeNode = coreChild;
            if (coreChild instanceof Presentation pres) {
                Color c = ColorTags.get(pres);
                if (c != null) {
                    treeNode = new ColorDecorator(coreChild, c);
                }
            }

            GraffTreeItem uiChild = new GraffTreeItem(treeNode);
            tree.createNode(uiParent, uiChild);

            // recurse only if model node is composite (Slide is composite -> will add SlideElements)
            buildSubtree(tree, uiChild, coreChild);
        }
    }
}
