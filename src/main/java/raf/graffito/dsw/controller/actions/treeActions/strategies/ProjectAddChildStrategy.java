package raf.graffito.dsw.controller.actions.treeActions.strategies;

import raf.graffito.dsw.core.model.decorator.ColorDecorator;
import raf.graffito.dsw.core.model.decorator.ColorManager;
import raf.graffito.dsw.core.model.decorator.NodeUtils;
import raf.graffito.dsw.core.model.factory.graffNode.ChildType;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite;
import raf.graffito.dsw.core.model.repository.implementation.Presentation;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.gui.swing.dialogs.ChooseChildTypeDialog;
import raf.graffito.dsw.gui.swing.dialogs.ChooseColorDialog;
import raf.graffito.dsw.gui.swing.tree.GraffTreeImplementation;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import java.awt.*;

public class ProjectAddChildStrategy implements AddElementStrategy {

    @Override
    public boolean supports(GraffNode parentNode) {
        return parentNode instanceof Project;
    }

    @Override
    public void add(GraffTreeImplementation tree, GraffTreeItem selected) throws Exception {
        GraffNode parentNode = selected.getGraffNode(); // is Project

        ChooseChildTypeDialog dlgType = new ChooseChildTypeDialog(MainFrame.getInstance());
        ChooseChildTypeDialog.Result res = dlgType.showDialog();
        if (res == ChooseChildTypeDialog.Result.CANCEL) return;

        boolean creatingPresentation = (res == ChooseChildTypeDialog.Result.PRESENTATION);

        Color chosenColor = null;

        if (creatingPresentation) {
            GraffNode projectCore = NodeUtils.owningProjectCore(parentNode);

            ChooseColorDialog dlgColor = new ChooseColorDialog(MainFrame.getInstance(), projectCore);
            dlgColor.setVisible(true);

            chosenColor = dlgColor.getSelectedColor();
            if (chosenColor == null) return;

            ColorManager.reserveColor(projectCore, chosenColor);
        }

        GraffNode child =
                creatingPresentation
                        ? tree.getRepo().addChild((GraffNodeComposite) parentNode, ChildType.PRESENTATION)
                        : tree.getRepo().addChild((GraffNodeComposite) parentNode, ChildType.SLIDE);

        if (creatingPresentation && child instanceof Presentation) {
            child = new ColorDecorator(child, chosenColor);
        }

        tree.createNode(selected, new GraffTreeItem(child));
    }
}
