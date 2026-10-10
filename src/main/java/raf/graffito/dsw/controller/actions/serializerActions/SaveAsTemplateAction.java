package raf.graffito.dsw.controller.actions.serializerActions;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.serializer.TemplateJsonSerializer;
import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;

public class SaveAsTemplateAction extends AbstractGraffAction {

    public SaveAsTemplateAction() {
        putValue(NAME, "Save as Template");
        putValue(SHORT_DESCRIPTION, "Save selected project as a template");
        putValue(SMALL_ICON, loadIcon("/images/templateIcon.png"));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = getProjectFromSelection();
        if (project == null) {
            JOptionPane.showMessageDialog(null,
                    "Select a project (or any child inside a project) to save as a template.",
                    "Template",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String defaultName = project.getName() == null ? "" : project.getName().trim();
        String name = (String) JOptionPane.showInputDialog(
                null,
                "Template name:",
                "Save Project as Template",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                defaultName
        );

        if (name == null) return;
        name = name.trim();
        if (name.isBlank()) {
            JOptionPane.showMessageDialog(null,
                    "Template name cannot be empty.",
                    "Template",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // If you store TemplateJsonSerializer somewhere else, swap this line accordingly.
            TemplateJsonSerializer serializer = ApplicationFramework.getInstance().getTemplateJsonSerializer();

            if (serializer.templateExists(name)) {
                int choice = JOptionPane.showConfirmDialog(
                        null,
                        "A template with that name already exists. Overwrite?",
                        "Confirm overwrite",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice != JOptionPane.YES_OPTION) return;
            }

            Path saved = serializer.saveTemplate(project, name);
            JOptionPane.showMessageDialog(null,
                    "Template saved:\n" + saved,
                    "Template",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Failed to save template:\n" + ex.getMessage(),
                    "Template",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generic resolver: uses the selected tree node and walks up until it finds a Project.
     * Replace this with your existing save project's resolver if you already have one.
     */
    private Project getProjectFromSelection() {
        try {
            // This is the most common pattern in your app: "selected node" comes from the tree implementation.
            GraffNode selected = MainFrame.getInstance().getGraffTree().getSelectedNode().getGraffNode();

            while (selected != null) {
                if (selected instanceof Project p) return p;
                selected = selected.getParent();
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
