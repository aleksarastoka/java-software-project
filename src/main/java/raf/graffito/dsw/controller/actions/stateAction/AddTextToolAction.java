package raf.graffito.dsw.controller.actions.stateAction;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.state.ToolType;

import java.awt.event.ActionEvent;

public class AddTextToolAction extends AbstractGraffAction {

    public AddTextToolAction() {
        putValue(NAME, "Add Text");
        putValue(SHORT_DESCRIPTION, "Add Text");
        putValue(SMALL_ICON, loadIcon("/images/addTextIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ApplicationFramework.getInstance().getToolStateManager().setActiveTool(ToolType.ADD_TEXT);
    }
}
