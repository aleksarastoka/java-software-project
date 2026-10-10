package raf.graffito.dsw.controller.actions.commandActions;
import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class UndoAction extends AbstractGraffAction {

    public UndoAction() {
        putValue(NAME, "Undo");
        putValue(SHORT_DESCRIPTION, "Undo last action");
        putValue(SMALL_ICON, loadIcon("/images/undoIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Slide slide = ApplicationFramework.getInstance()
                .getToolStateManager()
                .getActiveSlide();
        if (slide == null) return;

        slide.getCommandManager().undo();

        // ✅ refresh button state
        MainFrame.getInstance().getProjectView().updateUndoRedoState();
    }
}
