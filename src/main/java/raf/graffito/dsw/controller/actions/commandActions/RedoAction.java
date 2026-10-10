package raf.graffito.dsw.controller.actions.commandActions;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class RedoAction extends AbstractGraffAction {

    public RedoAction() {
        putValue(NAME, "Redo");
        putValue(SHORT_DESCRIPTION, "Redo last undone action");
        putValue(SMALL_ICON, loadIcon("/images/redoIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Slide slide = ApplicationFramework.getInstance()
                .getToolStateManager()
                .getActiveSlide();
        if (slide == null) return;

        slide.getCommandManager().redo();

        // ✅ refresh button state
        MainFrame.getInstance().getProjectView().updateUndoRedoState();
    }
}
