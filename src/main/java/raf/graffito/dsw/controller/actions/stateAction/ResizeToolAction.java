package raf.graffito.dsw.controller.actions.stateAction;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.state.ToolType;

import java.awt.event.ActionEvent;

public class ResizeToolAction extends AbstractGraffAction {

    public ResizeToolAction() {
        putValue(NAME, "Resize");
        putValue(SHORT_DESCRIPTION, "Resize");
        putValue(SMALL_ICON, loadIcon("/images/resizeIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ApplicationFramework.getInstance().getToolStateManager().setActiveTool(ToolType.RESIZE);
    }
}
