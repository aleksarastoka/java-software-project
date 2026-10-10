package raf.graffito.dsw.controller.actions.stateAction;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.state.ToolType;
import raf.graffito.dsw.core.state.tools.CloneToolState;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;
import raf.graffito.dsw.gui.swing.views.editor.SlideView;

import java.awt.*;
import java.awt.event.ActionEvent;

public class CloneToolAction extends AbstractGraffAction {

    public CloneToolAction() {
        putValue(SMALL_ICON, loadIcon("/images/cloneIcon.png"));
        putValue(NAME, "Clone");
        putValue(SHORT_DESCRIPTION, "Clone selected elements (and then click to clone)");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Component c = MainFrame.getInstance().getProjectView().getTabs().getSelectedComponent();
        if (c == null) {
            ApplicationFramework.getInstance().getMg().generate("CHOOSE_SLIDE");
            return;
        }

        ApplicationFramework.getInstance().getToolStateManager().setActiveTool(ToolType.CLONE);

        SlideView sv = MainFrame.getInstance().getProjectView().getTabs().getActiveSlideView();
        if (sv == null) return;

        var mgr = ApplicationFramework.getInstance().getToolStateManager();
        var st = mgr.getTools().get(ToolType.CLONE);
        if (st instanceof CloneToolState cts) {
            cts.cloneSelectedNow(sv.getSlide());
        }
    }
}
