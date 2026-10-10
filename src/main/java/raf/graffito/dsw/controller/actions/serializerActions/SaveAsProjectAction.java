package raf.graffito.dsw.controller.actions.serializerActions;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.serializer.ProjectFileRegistry;
import raf.graffito.dsw.core.serializer.ProjectJsonSerializer;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;

public class SaveAsProjectAction extends AbstractGraffAction {

    private final ProjectJsonSerializer serializer;

    public SaveAsProjectAction() {
        putValue(NAME, "Save As...");
        putValue(SHORT_DESCRIPTION, "Save the current project to a JSON file");
        putValue(SMALL_ICON, loadIcon("/images/saveProjectAsIcon.png"));// optional
        serializer = ApplicationFramework.getInstance().getProjectJsonSerializer();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = getSelectedProject();
        if (project == null) {
            JOptionPane.showMessageDialog(null,
                    "Please select a Project in the tree (or any node inside it) before saving.",
                    "Save As",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Project As");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new FileNameExtensionFilter("Graffito Project (*.json)", "json"));

        // sensible default name
        fc.setSelectedFile(new File(safeFileName(project.getName()) + ".json"));

        int choice = fc.showSaveDialog(null);
        if (choice != JFileChooser.APPROVE_OPTION) return;

        File selected = fc.getSelectedFile();
        if (selected == null) return;

        // enforce .json extension
        if (!selected.getName().toLowerCase().endsWith(".json")) {
            selected = new File(selected.getParentFile(), selected.getName() + ".json");
        }

        // confirm overwrite
        if (selected.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(null,
                    "File already exists.\nOverwrite?\n\n" + selected.getAbsolutePath(),
                    "Confirm overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (overwrite != JOptionPane.YES_OPTION) return;
        }

        try {
            Path path = selected.toPath();
            serializer.save(project, path);
            ProjectFileRegistry.setPath(project, path);

            // IMPORTANT: SaveAs typically updates the project "file path" in app state.
            // If you already have a place to store it, set it here.
            // e.g. project.setFilePath(path.toString()); or ApplicationFramework.getInstance().setProjectPath(project, path);

            JOptionPane.showMessageDialog(null,
                    "Project saved:\n" + selected.getAbsolutePath(),
                    "Save As",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to save project:\n" + ex.getMessage(),
                    "Save As",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Project getSelectedProject() {
        // 1) Get the selected tree item
        GraffTreeItem selected = MainFrame.getInstance().getGraffTree().getSelectedNode();

        if (selected == null) return null;

        // 2) Walk up until we find a Project
        GraffTreeItem curr = selected;
        while (curr != null) {
            if (curr.getGraffNode() instanceof Project p) {
                return p;
            }
            if (curr.getParent() instanceof GraffTreeItem parent) {
                curr = parent;
            } else {
                curr = null;
            }
        }

        return null;
    }


    private static String safeFileName(String name) {
        if (name == null || name.isBlank()) return "project";
        return name.replaceAll("[\\\\/:*?\"<>|]+", "_").trim();
    }
}
