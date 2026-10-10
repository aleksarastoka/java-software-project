package raf.graffito.dsw.controller.actions.treeActions.strategies;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.gui.swing.tree.GraffTreeImplementation;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;

public class DefaultAddChildStrategy implements AddElementStrategy {

    @Override
    public boolean supports(GraffNode parentNode) {
        return true; // fallback
    }

    @Override
    public void add(GraffTreeImplementation tree, GraffTreeItem selected) {
        tree.addChild(selected);
    }
}
