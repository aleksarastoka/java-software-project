package raf.graffito.dsw.gui.swing.views.main;

import raf.graffito.dsw.controller.ActionManager;
import raf.graffito.dsw.controller.mediator.ScreenSizeMediator;
import raf.graffito.dsw.gui.swing.views.mediator.ScreenSizeMediatorPanel;

import javax.swing.*;

public class MyToolBar extends JToolBar {

    public MyToolBar(ActionManager actionManager, ScreenSizeMediator screenMediator) {
        super(HORIZONTAL);
        setFloatable(false);

        add(actionManager.getExitAction());
        add(actionManager.getAddElementAction());
        add(actionManager.getEditProjectAction());
        add(actionManager.getDeleteElementAction());
        add(actionManager.getSaveProjectAction());
        add(actionManager.getSaveAsProjectAction());
        add(actionManager.getSaveAsTemplateAction());
        add(actionManager.getLoadProjectAction());
        add(actionManager.getAboutUsAction());

        addSeparator();
        add(new ScreenSizeMediatorPanel(screenMediator));
    }
}
