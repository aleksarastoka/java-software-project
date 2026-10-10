package raf.graffito.dsw.controller.actions.treeActions;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.gui.swing.dialogs.ProjectEditDialog;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class EditProjectAction extends AbstractGraffAction {

    public EditProjectAction() {
        putValue(NAME, "Edit Project");
        putValue(SHORT_DESCRIPTION, "Change the selected project's author or title");
        putValue(SMALL_ICON, loadIcon("/images/editProjectIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        var tree = MainFrame.getInstance().getGraffTree();
        var selectedObj = tree.getSelectedNode();
        if (!(selectedObj instanceof GraffTreeItem item)) {
            ApplicationFramework.getInstance().getMg().generate("NO_NODE_SELECTED");
            return;
        }

        Object model = item.getGraffNode();
        if (!(model instanceof Project project)) {
            ApplicationFramework.getInstance().getMg().generate("NODE_IS_NOT_PROJECT");
            return;
        }

        var dlg = new ProjectEditDialog(MainFrame.getInstance(), project.getTitle(), project.getAuthor());
        var res = dlg.showDialog();
        if (res == null) return;

        String newTitle = res.title.trim();
        String newAuthor = res.author.trim();
        if (newTitle.isEmpty()) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    "Title cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Model: apply updates (or call a repository/service method)
        boolean changed = false;
        if (!newTitle.equals(project.getTitle()))  { project.setTitle(newTitle); changed = true; }
        if (!newAuthor.equals(project.getAuthor())) { project.setAuthor(newAuthor); changed = true; }

        if (changed) {
            project.setAuthor(newAuthor);
            project.setTitle(newTitle);
        }
    }

}
