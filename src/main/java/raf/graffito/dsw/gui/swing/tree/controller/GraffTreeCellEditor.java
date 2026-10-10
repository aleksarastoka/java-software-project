// raf/graffito/dsw/gui/swing/tree/controller/GraffTreeCellEditor.java
package raf.graffito.dsw.gui.swing.tree.controller;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Workspace;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import static raf.graffito.dsw.core.model.decorator.Decorators.unwrap;

public class GraffTreeCellEditor extends DefaultTreeCellEditor implements ActionListener {
    private Object clickedOn = null;
    private JTextField edit = null;
    private String pendingNewName = null;
    private String oldName = null;

    public GraffTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree,
                                                Object value,
                                                boolean selected,
                                                boolean expanded,
                                                boolean leaf,
                                                int row) {
        clickedOn = value;

        String initial = (value == null) ? "" : value.toString();

        if (value instanceof DefaultMutableTreeNode dmtn && dmtn instanceof GraffTreeItem gti) {
            Object domain = gti.getGraffNode();
            if (domain instanceof GraffNode gn) {
                GraffNode core = unwrap(gn);
                oldName = core.getName();
                if (oldName != null) initial = oldName;
            }
        }

        edit = new JTextField(initial);
        edit.addActionListener(this);
        return edit;
    }

    @Override
    public boolean isCellEditable(EventObject event) {
        if (event instanceof MouseEvent) {
            return ((MouseEvent) event).getClickCount() >= 3; // triple-click
        }
        return false;
    }

    @Override
    public Object getCellEditorValue() {
        return (edit != null) ? edit.getText() : null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!(clickedOn instanceof DefaultMutableTreeNode dmtn) || !(dmtn instanceof GraffTreeItem gti)) {
            cancelCellEditing();
            return;
        }

        String typed = edit.getText().trim();

        // Don’t allow empty names (optional, but usually desirable)
        if (typed.isEmpty()) {
            ApplicationFramework.getInstance().getMg().generate("NAME_CANNOT_BE_EMPTY");
            cancelCellEditing();
            return;
        }

        Object domain = gti.getGraffNode();
        if (!(domain instanceof GraffNode gn)) {
            cancelCellEditing();
            return;
        }

        GraffNode core = unwrap(gn);

        // Block Workspace rename
        if (core instanceof raf.graffito.dsw.core.model.repository.implementation.Workspace) {
            ApplicationFramework.getInstance().getMg().generate("NODE_CANNOT_BE_RENAMED");
            cancelCellEditing();
            return;
        }

        // If the name didn’t change, just commit UI edit silently
        String currentName = core.getName();
        if (currentName != null && currentName.equals(typed)) {
            pendingNewName = typed;
            stopCellEditing();
            return;
        }

        // === Uniqueness check across entire hierarchy ===
        GraffNode root = getHierarchyRoot(core);
        if (root != null) {
            GraffNode found = root.findByName(typed); // your recursive search
            if (found != null && found != core) {
                // Build a friendlier type label for the message (Project/Presentation/Slide/etc.)
                String kind = readableType(core);
                // Use your Message Generator (adjust the key/text if your MG expects a code)
                ApplicationFramework.getInstance().getMg()
                        .generate("DUPLICATE_NODE_NAME");
                cancelCellEditing();
                return;
            }
        }

        // ok to rename → remember and commit
        pendingNewName = typed;
        stopCellEditing();
    }

    /** Climb to the top-most GraffNode (usually Workspace). */
    private GraffNode getHierarchyRoot(GraffNode node) {
        GraffNode n = node;
        while (n != null && n.getParent() instanceof GraffNode) {
            n = (GraffNode) n.getParent();
        }
        return n;
    }

    private String readableType(GraffNode node) {
        Object n = node;
        if (n instanceof raf.graffito.dsw.core.model.repository.implementation.Project) return "project";
        if (n instanceof raf.graffito.dsw.core.model.repository.implementation.Presentation) return "presentation";
        if (n instanceof raf.graffito.dsw.core.model.repository.implementation.Slide) return "slide";
        if (n instanceof raf.graffito.dsw.core.model.repository.implementation.Workspace) return "workspace";
        return "node";
    }

    @Override
    public boolean stopCellEditing() {
        try {
            if (pendingNewName != null && clickedOn instanceof DefaultMutableTreeNode dmtn && dmtn instanceof GraffTreeItem gti) {
                Object domain = gti.getGraffNode();
                if (domain instanceof GraffNode gn) {
                    GraffNode core = unwrap(gn);
                    String before = (oldName != null) ? oldName : core.getName();
                    core.setName(pendingNewName);

                    // repaint this row
                    ((javax.swing.tree.DefaultTreeModel) tree.getModel()).nodeChanged(dmtn);

                    // notify observers via tree implementation
                    MainFrame.getInstance().getGraffTree()
                            .notifyNodeRenamed(core, before, pendingNewName);
                }
            }
            return super.stopCellEditing();
        } finally {
            pendingNewName = null;
            oldName = null;
        }
    }
}
