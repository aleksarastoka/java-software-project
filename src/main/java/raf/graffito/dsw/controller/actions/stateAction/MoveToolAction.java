package raf.graffito.dsw.controller.actions.stateAction;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.state.ToolType;

import java.awt.event.ActionEvent;

public class MoveToolAction extends AbstractGraffAction {

    public MoveToolAction() {
        putValue(NAME, "Move");
        putValue(SHORT_DESCRIPTION, "Move");
        putValue(SMALL_ICON, loadIcon("/images/moveIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ApplicationFramework.getInstance().getToolStateManager().setActiveTool(ToolType.MOVE);
    }
}
