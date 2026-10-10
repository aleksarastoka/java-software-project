package raf.graffito.dsw.controller.actions.treeActions;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.decorator.ColorDecorator;
import raf.graffito.dsw.core.model.decorator.ColorManager;
import raf.graffito.dsw.core.model.decorator.NodeUtils;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.gui.swing.tree.GraffTreeImplementation;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import java.awt.event.ActionEvent;

public class DeleteElementAction extends AbstractGraffAction {

    public DeleteElementAction() {
        putValue(NAME, "Delete Element");
        putValue(SHORT_DESCRIPTION, "Delete an element from the tree");
        putValue(SMALL_ICON, loadIcon("/images/deleteIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GraffTreeImplementation tree = MainFrame.getInstance().getGraffTree();

        GraffTreeItem selected = tree.getSelectedNode();
        if (selected == null) {
            ApplicationFramework.getInstance().getMg().generate("NO_NODE_SELECTED");
            return;
        }

        GraffNode node = selected.getGraffNode();
        if (node instanceof SlideElement){
            ApplicationFramework.getInstance().getMg().generate("CANNOT_DELETE_SLIDE_ELEMENT_VIA_TOOL_BAR");
            return;
        }
        if (node instanceof ColorDecorator dec) {

            GraffNode projectCore = NodeUtils.owningProjectCore(dec.getDecoratedNode());
            if (projectCore != null) {
                ColorManager.releaseColor(projectCore, dec.getColor());
            }
        }

        tree.deleteChild(selected);
    }
}
