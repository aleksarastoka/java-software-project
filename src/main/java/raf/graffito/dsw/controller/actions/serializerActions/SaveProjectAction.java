package raf.graffito.dsw.controller.actions.serializerActions;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.serializer.ProjectFileRegistry;
import raf.graffito.dsw.core.serializer.ProjectJsonSerializer;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;

public class SaveProjectAction extends AbstractGraffAction {

    private final ProjectJsonSerializer serializer;

    public SaveProjectAction() {
        putValue(NAME, "Save");
        putValue(SHORT_DESCRIPTION, "Save the selected project");
        putValue(SMALL_ICON,loadIcon("/images/saveProjectIcon.png"));
        serializer = ApplicationFramework.getInstance().getProjectJsonSerializer();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = getSelectedProject();
        if (project == null) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    "Select a Project (or any node inside it) in the tree before saving.",
                    "Save",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Path existing = ProjectFileRegistry.getPath(project);

        // If we don't know where to save yet -> behave like Save As
        if (existing == null) {
            // Call your SaveAs action directly if you have it in ActionManager,
            // OR just do the same SaveAs logic here.
            // I’ll do the clean way: trigger SaveAs action.
            MainFrame.getInstance()
                    .getActionManager()
                    .getSaveAsProjectAction()
                    .actionPerformed(e);
            return;
        }

        try {
            serializer.save(project, existing);
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    "Project saved:\n" + existing,
                    "Save",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    "Failed to save project:\n" + ex.getMessage(),
                    "Save",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Project getSelectedProject() {
        GraffTreeItem selected = MainFrame.getInstance()
                .getGraffTree()
                .getSelectedNode();

        if (selected == null) return null;

        GraffTreeItem curr = selected;
        while (curr != null) {
            if (curr.getGraffNode() instanceof Project p) return p;
            if (curr.getParent() instanceof GraffTreeItem parent) curr = parent;
            else curr = null;
        }
        return null;
    }
}
