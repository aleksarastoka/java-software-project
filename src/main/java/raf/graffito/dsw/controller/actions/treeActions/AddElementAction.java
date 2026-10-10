package raf.graffito.dsw.controller.actions.treeActions;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.controller.actions.treeActions.strategies.AddElementStrategy;
import raf.graffito.dsw.controller.actions.treeActions.strategies.DefaultAddChildStrategy;
import raf.graffito.dsw.controller.actions.treeActions.strategies.ProjectAddChildStrategy;
import raf.graffito.dsw.controller.actions.treeActions.strategies.WorkspaceAddProjectStrategy;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.gui.swing.tree.GraffTreeImplementation;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class AddElementAction extends AbstractGraffAction {

    private final List<AddElementStrategy> strategies;

    public AddElementAction() {
        putValue(NAME, "Add an Element");
        putValue(SHORT_DESCRIPTION, "Add a Project, Presentation or Slide");
        putValue(SMALL_ICON, loadIcon("/images/addButton.png"));

        this.strategies = List.of(
                new WorkspaceAddProjectStrategy(), // workspace => empty/template project
                new ProjectAddChildStrategy(),     // project => presentation/slide + color logic
                new DefaultAddChildStrategy()      // fallback => tree.addChild
        );
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        GraffTreeImplementation tree = MainFrame.getInstance().getGraffTree();

        GraffTreeItem selected = tree.getSelectedNode();
        if (selected == null) {
            ApplicationFramework.getInstance().getMg().generate("NO_NODE_SELECTED");
            return;
        }
        GraffNode parentNode = selected.getGraffNode();

        if (parentNode instanceof Slide){
            ApplicationFramework.getInstance().getMg().generate("NODE_CANNOT_BE_ADDED");
            return;
        }

        try {

            for (AddElementStrategy s : strategies) {
                if (s.supports(parentNode)) {
                    s.add(tree, selected);
                    return;
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    "Error adding child:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
