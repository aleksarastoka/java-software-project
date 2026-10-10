package raf.graffito.dsw.controller.actions.stateAction;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.state.ToolType;

import java.awt.event.ActionEvent;

public class AddImageToolAction extends AbstractGraffAction {

    public AddImageToolAction() {
        putValue(NAME, "Add Image");
        putValue(SHORT_DESCRIPTION, "Add Image");
        putValue(SMALL_ICON, loadIcon("/images/addImageIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ApplicationFramework.getInstance().getToolStateManager().setActiveTool(ToolType.ADD_IMAGE);
    }
}
