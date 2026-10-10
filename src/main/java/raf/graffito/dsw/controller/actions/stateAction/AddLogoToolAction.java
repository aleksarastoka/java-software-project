package raf.graffito.dsw.controller.actions.stateAction;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.state.ToolType;

import java.awt.event.ActionEvent;

public class AddLogoToolAction extends AbstractGraffAction {

    public AddLogoToolAction() {
        putValue(NAME, "Add Logo");
        putValue(SHORT_DESCRIPTION, "Add Logo");
        putValue(SMALL_ICON, loadIcon("/images/addLogoIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ApplicationFramework.getInstance().getToolStateManager().setActiveTool(ToolType.ADD_LOGO);
    }
}
