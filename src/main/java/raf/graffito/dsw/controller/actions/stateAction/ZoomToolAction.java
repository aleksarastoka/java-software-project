package raf.graffito.dsw.controller.actions.stateAction;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.state.ToolType;

import java.awt.event.ActionEvent;

public class ZoomToolAction extends AbstractGraffAction {

    public ZoomToolAction() {
        putValue(NAME, "Zoom");
        putValue(SHORT_DESCRIPTION, "Zoom an element");
        putValue(SMALL_ICON, loadIcon("/images/zoomIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ApplicationFramework.getInstance().getToolStateManager().setActiveTool(ToolType.ZOOM);
    }
}
