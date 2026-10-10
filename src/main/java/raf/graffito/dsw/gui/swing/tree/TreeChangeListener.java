package raf.graffito.dsw.gui.swing.tree;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;

public interface TreeChangeListener {
    void nodeAdded(GraffNode parent, GraffNode child, int index);
    void nodeRemoved(GraffNode parent, GraffNode child, int formerIndex);
    void nodeRenamed(GraffNode node, String oldName, String newName);
    void orderChanged(GraffNode node);
}