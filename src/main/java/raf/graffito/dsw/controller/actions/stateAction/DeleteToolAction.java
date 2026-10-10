package raf.graffito.dsw.controller.actions.stateAction;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.state.ToolType;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;


import java.awt.*;
import java.awt.event.ActionEvent;

public class DeleteToolAction extends AbstractGraffAction {
    public DeleteToolAction() {
        putValue(SMALL_ICON, loadIcon("/images/deleteToolIcon.png"));
        putValue(NAME, "Delete element");
        putValue(SHORT_DESCRIPTION, "Delete slide element");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Slide slide = MainFrame.getInstance().getProjectView().getActiveSlide();
        if (slide == null) {
            ApplicationFramework.getInstance().getMg().generate("CHOOSE_SLIDE");
            return;
        }

        var mgr = ApplicationFramework.getInstance().getToolStateManager();
        mgr.setActiveTool(ToolType.DELETE);

        if (!mgr.getSelectedElementsReadOnly().isEmpty()) {
            var deleteTool = (raf.graffito.dsw.core.state.tools.DeleteToolState) mgr.getCurrent();
            deleteTool.deleteSelection(slide);
        }
    }


}
