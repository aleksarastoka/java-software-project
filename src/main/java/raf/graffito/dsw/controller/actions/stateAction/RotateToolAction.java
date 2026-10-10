package raf.graffito.dsw.controller.actions.stateAction;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.state.ToolType;

import java.awt.event.ActionEvent;

public class RotateToolAction extends AbstractGraffAction {

    public RotateToolAction() {
        putValue(NAME, "Rotate");
        putValue(SHORT_DESCRIPTION, "Rotate");
        putValue(SMALL_ICON, loadIcon("/images/rotateIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ApplicationFramework.getInstance().getToolStateManager().setActiveTool(ToolType.ROTATE);
    }
}
