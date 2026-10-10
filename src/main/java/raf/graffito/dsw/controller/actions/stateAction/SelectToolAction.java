package raf.graffito.dsw.controller.actions.stateAction;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.state.ToolType;

import java.awt.event.ActionEvent;

public class SelectToolAction extends AbstractGraffAction {

    public SelectToolAction() {
        putValue(NAME, "Select");
        putValue(SHORT_DESCRIPTION, "Select");
        putValue(SMALL_ICON, loadIcon("/images/selectIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ApplicationFramework.getInstance().getToolStateManager().setActiveTool(ToolType.SELECT);
    }
}
