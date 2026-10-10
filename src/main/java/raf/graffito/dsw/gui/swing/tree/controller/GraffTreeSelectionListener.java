package raf.graffito.dsw.gui.swing.tree.controller;

import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

public class GraffTreeSelectionListener implements TreeSelectionListener {
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getPath();
        GraffTreeItem item = (GraffTreeItem)path.getLastPathComponent();
        System.out.println("Selektovan cvor: " + item.toString());
        System.out.println("getPath: "+e.getPath());
    }
}
