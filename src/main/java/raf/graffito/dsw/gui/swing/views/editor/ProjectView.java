package raf.graffito.dsw.gui.swing.views.editor;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.controller.ActionManager;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Presentation;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.gui.swing.color.ColorTags;
import raf.graffito.dsw.gui.swing.tree.TreeChangeListener;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;
import raf.graffito.dsw.gui.swing.views.sidebar.RightSidebarPanel;

import javax.swing.*;
import java.awt.*;

import static raf.graffito.dsw.core.model.decorator.Decorators.unwrap;

@Getter @Setter
public class ProjectView extends JPanel implements TreeChangeListener {

    private Project currentProject;
    private SlidesTabbedView tabs;
    private ActionManager actionManager;

    private final JLabel header = new JLabel("Editor");

    private final RightSidebarPanel rightSidebarPanel;
    private final ProjectBottomPanel bottomPanel;

    private boolean editingLock = false;

    public ProjectView(ActionManager am) {
        super(new BorderLayout());

        JPanel north = new JPanel(new BorderLayout());
        north.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        north.add(header, BorderLayout.WEST);
        add(north, BorderLayout.NORTH);

        actionManager = am;

        rightSidebarPanel = new RightSidebarPanel(am);
        add(rightSidebarPanel, BorderLayout.EAST);

        bottomPanel = new ProjectBottomPanel(am);
        add(bottomPanel, BorderLayout.SOUTH);

        rightSidebarPanel.setEnabledForSlide(false);
        updateUndoRedoState();
    }

    public void setEditingLock(boolean locked) {
        this.editingLock = locked;



        // zaključaj menu/toolbar/shortcuts
        this.actionManager.getUndoAction().setEnabled(!locked);
        this.actionManager.getRedoAction().setEnabled(!locked);

        // zaključaj dodavanje textbox-a (prilagodi naziv gettera ako se kod tebe drugačije zove)
        this.actionManager.getAddTextToolAction().setEnabled(!locked);

        // zaključaj i bottom panel dugmad (jer ona imaju sopstveni enabled)
        if (locked && bottomPanel != null) bottomPanel.updateEnabled(false, false);
        if (!locked) updateUndoRedoState();
    }

    public void showProject(Project project) {
        this.currentProject = project;
        header.setText(project != null && project.getName() != null ? project.getName() : "Editor");

        if (tabs != null) remove(tabs);

        if (project == null) {
            rightSidebarPanel.setEnabledForSlide(false);
            updateUndoRedoState();
            revalidate();
            repaint();
            return;
        }

        tabs = new SlidesTabbedView(project, this);
        add(tabs, BorderLayout.CENTER);

        rightSidebarPanel.setEnabledForSlide(tabs.hasRealSlideSelected());
        updateUndoRedoState();

        revalidate();
        repaint();
    }

    public Slide getActiveSlide() {
        return (tabs == null) ? null : tabs.getActiveSlide();
    }

    public void updateUndoRedoState() {
        if (editingLock) {
            this.actionManager.getUndoAction().setEnabled(false);
            this.actionManager.getRedoAction().setEnabled(false);
            if (bottomPanel != null) bottomPanel.updateEnabled(false, false);
            return;
        }

        Slide slide = ApplicationFramework.getInstance()
                .getToolStateManager()
                .getActiveSlide();

        boolean canUndo = false;
        boolean canRedo = false;

        if (slide != null && slide.getCommandManager() != null) {
            canUndo = slide.getCommandManager().canUndo();
            canRedo = slide.getCommandManager().canRedo();
        }


        this.actionManager.getUndoAction().setEnabled(canUndo);
        this.actionManager.getRedoAction().setEnabled(canRedo);

        if (bottomPanel != null) bottomPanel.updateEnabled(canUndo, canRedo);
    }

    public void refreshActiveSlideHeader() {
        if (tabs == null) return;
        tabs.refreshActiveTabHeader();
    }

    // ===== TreeChangeListener =====

    @Override
    public void nodeAdded(GraffNode parent, GraffNode child, int index) {
        if (!isInCurrentProject(child)) return;

        child = unwrap(child);
        parent = unwrap(parent);

        if (tabs == null || currentProject == null) return;

        if (child instanceof Slide slide && parent instanceof Presentation pres) {
            Color color = ColorTags.get(pres);
            SwingUtilities.invokeLater(() -> {
                tabs.removeEmptyPlaceholderIfPresent();
                tabs.addSlideTab(pres, slide, color, index);
                rightSidebarPanel.setEnabledForSlide(tabs.hasRealSlideSelected());
                updateUndoRedoState();
            });
            return;
        }

        if (child instanceof Slide slide && parent instanceof Project proj) {
            if (unwrap(proj) != unwrap(currentProject)) return;

            Color color = ColorTags.get(currentProject);
            SwingUtilities.invokeLater(() -> {
                tabs.removeEmptyPlaceholderIfPresent();
                tabs.addSlideTab(null, slide, color, index);
                rightSidebarPanel.setEnabledForSlide(tabs.hasRealSlideSelected());
                updateUndoRedoState();
            });
        }
    }

    @Override
    public void nodeRemoved(GraffNode parent, GraffNode child, int formerIndex) {
        parent = unwrap(parent);
        child = unwrap(child);

        if (currentProject == null || tabs == null) return;

        if (child instanceof Project) {
            if (child == currentProject || child.equals(currentProject)) {
                currentProject = null;
                remove(tabs);
                tabs = null;
                header.setText("Editor");
                rightSidebarPanel.setEnabledForSlide(false);
                updateUndoRedoState();
                revalidate();
                repaint();
            }
            return;
        }

        if (!isInCurrentProject(parent)) return;

        if (child instanceof Slide || child instanceof Presentation) {
            Project p = this.currentProject;
            SwingUtilities.invokeLater(() -> showProject(p));
        }
    }

    @Override
    public void nodeRenamed(GraffNode node, String oldName, String newName) {
        if (!isInCurrentProject(node)) return;
        node = unwrap(node);
        if (tabs == null) return;

        if (node == currentProject) {
            header.setText(newName != null ? newName : "Editor");
        } else if (node instanceof Presentation pres) {
            SwingUtilities.invokeLater(() -> tabs.renamePresentationTabs(pres));
        } else if (node instanceof Slide slide) {
            SwingUtilities.invokeLater(() -> tabs.renameSlideTab(slide));
        }
    }

    @Override
    public void orderChanged(GraffNode node) {
        if (currentProject == null) return;
        GraffNode p = unwrap(node);
        if (!isInCurrentProject(p)) return;

        Project proj = this.currentProject;
        SwingUtilities.invokeLater(() -> showProject(proj));
    }

    private boolean isInCurrentProject(GraffNode node) {
        if (currentProject == null || node == null) return false;
        GraffNode n = unwrap(node);
        while (n != null) {
            if (n == currentProject) return true;
            Object p = n.getParent();
            n = (p instanceof GraffNode) ? (GraffNode) p : null;
        }
        return false;
    }
}
