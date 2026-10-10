package raf.graffito.dsw.controller.actions.uiActions;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.gui.swing.views.dialogs.AboutUsFrame;

import java.awt.event.ActionEvent;

public class AboutUsAction extends AbstractGraffAction {

    public AboutUsAction() {
        this.putValue(NAME, "About us");
        this.putValue(SHORT_DESCRIPTION, "About us");
        this.putValue(SMALL_ICON, loadIcon("/images/aboutus.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new AboutUsFrame();
    }
}
