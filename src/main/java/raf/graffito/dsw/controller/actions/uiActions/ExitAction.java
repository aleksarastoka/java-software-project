package raf.graffito.dsw.controller.actions.uiActions;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ExitAction extends AbstractGraffAction {
    public ExitAction() {
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK)); // Ovo je prečica za izlaz
        putValue(SMALL_ICON, loadIcon("/images/exit.png")); // Postavljanje ikonice
        putValue(NAME, "Exit"); // Ime akcije
        putValue(SHORT_DESCRIPTION, "Exit"); // Tooltip
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }
}
