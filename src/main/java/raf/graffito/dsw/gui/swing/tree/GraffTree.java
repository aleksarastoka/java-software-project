package raf.graffito.dsw.gui.swing.tree;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Workspace;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.tree.view.GraffTreeView;

public interface GraffTree {
    GraffTreeView generateTree(Workspace workspace);
    void addChild(GraffTreeItem item);
    GraffTreeItem getSelectedNode();
    GraffTreeItem findGraffTreeItem(GraffTreeItem item, GraffNode graffNode);
    GraffTreeItem getRoot();
    void createNode(GraffTreeItem parentItem, GraffTreeItem childItem);
    void removeNode(GraffTreeItem item);
    void deleteChild(GraffTreeItem item);

}
