package raf.graffito.dsw.controller.actions.treeActions.strategies;

import raf.graffito.dsw.core.model.decorator.ColorDecorator;
import raf.graffito.dsw.core.model.decorator.Decorators;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite;
import raf.graffito.dsw.core.model.repository.implementation.Presentation;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.serializer.TemplateJsonSerializer;
import raf.graffito.dsw.gui.swing.color.ColorTags;
import raf.graffito.dsw.gui.swing.tree.GraffTreeImplementation;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WorkspaceAddProjectStrategy implements AddElementStrategy {

    private final TemplateJsonSerializer templateSerializer;

    public WorkspaceAddProjectStrategy() {
        this.templateSerializer = new TemplateJsonSerializer();
    }

    @Override
    public boolean supports(GraffNode parentNode) {
        if (!(parentNode instanceof GraffNodeComposite)) return false;
        if (parentNode.getParent() != null) return false;

        return !(parentNode instanceof Project)
                && !(parentNode instanceof Presentation)
                && !(parentNode instanceof Slide);
    }

    @Override
    public void add(GraffTreeImplementation tree, GraffTreeItem selected) throws Exception {
        Object[] options = {"Empty Project", "From Template", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                MainFrame.getInstance(),
                "Create a new empty project, or create from a template?",
                "Add Project",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) return;

        if (choice == 0) {
            tree.addChild(selected);
            return;
        }

        List<String> templates = templateSerializer.listTemplates();
        if (templates == null || templates.isEmpty()) {
            JOptionPane.showMessageDialog(
                    MainFrame.getInstance(),
                    "No templates found in resources/templates.",
                    "Templates",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String picked = (String) JOptionPane.showInputDialog(
                MainFrame.getInstance(),
                "Choose a template:",
                "Templates",
                JOptionPane.PLAIN_MESSAGE,
                null,
                templates.toArray(new String[0]),
                templates.get(0)
        );

        if (picked == null || picked.isBlank()) return;

        Project loaded = templateSerializer.loadTemplate(picked);

        // Add root node
        GraffTreeItem projectItem = new GraffTreeItem(loaded);
        tree.createNode(selected, projectItem);

        // ✅ Build children into JTree (and decorate presentations with color dot)
        buildSubtree(tree, projectItem, loaded);
    }

    private void buildSubtree(GraffTreeImplementation tree, GraffTreeItem parentItem, GraffNode parentNode) {
        GraffNode coreParent = Decorators.unwrap(parentNode);
        if (!(coreParent instanceof GraffNodeComposite comp)) return;
        if (comp.getChildren() == null) return;

        for (GraffNode child : comp.getChildren()) {
            GraffNode coreChild = Decorators.unwrap(child);

            // ✅ if this is a Presentation, wrap it in ColorDecorator so renderer draws the color symbol
            GraffNode treeNode = coreChild;
            if (coreChild instanceof Presentation pres) {
                Color c = ColorTags.get(pres); // mapper should have set this from DTO
                if (c != null) treeNode = new ColorDecorator(pres, c);
            }

            GraffTreeItem childItem = new GraffTreeItem(treeNode);
            tree.createNode(parentItem, childItem);

            // recurse using the CORE node (not the decorator)
            buildSubtree(tree, childItem, coreChild);
        }
    }
}
