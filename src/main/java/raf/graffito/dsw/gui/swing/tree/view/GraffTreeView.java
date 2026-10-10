package raf.graffito.dsw.gui.swing.tree.view;

import raf.graffito.dsw.gui.swing.tree.controller.GraffTreeCellEditor;
import raf.graffito.dsw.gui.swing.tree.controller.GraffTreeSelectionListener;
import raf.graffito.dsw.gui.swing.tree.dragndrop.SlideReorderTransferHandler;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

public class GraffTreeView extends JTree {

    public GraffTreeView(DefaultTreeModel treeModel) {
        setModel(treeModel);
        GraffTreeCellRenderer graffTreeCellRenderer = new GraffTreeCellRenderer();
        setCellRenderer(graffTreeCellRenderer);
        setCellEditor(new GraffTreeCellEditor(this, graffTreeCellRenderer));
        addTreeSelectionListener(new GraffTreeSelectionListener());
        setEditable(true);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setDropMode(DropMode.INSERT);
        setTransferHandler(new SlideReorderTransferHandler(this));
    }
}
