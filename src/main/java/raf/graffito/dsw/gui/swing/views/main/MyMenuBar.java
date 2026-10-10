package raf.graffito.dsw.gui.swing.views.main;



import raf.graffito.dsw.controller.ActionManager;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class MyMenuBar extends JMenuBar {
    public MyMenuBar(ActionManager actionManager) {
        JMenu fileMenu = new JMenu("File");
        JMenu infoMenu = new JMenu("Info");
        JMenu workspaceMenu = new JMenu("Workspace");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(actionManager.getSaveProjectAction());
        fileMenu.add(actionManager.getSaveAsProjectAction());
        fileMenu.add(actionManager.getSaveAsTemplateAction());
        fileMenu.add(actionManager.getLoadProjectAction());
        fileMenu.add(actionManager.getExitAction());
        infoMenu.add(actionManager.getAboutUsAction());
        workspaceMenu.add(actionManager.getAddElementAction());
        workspaceMenu.add(actionManager.getEditProjectAction());
        workspaceMenu.add(actionManager.getDeleteElementAction());
        add(fileMenu);
        add(workspaceMenu);
        add(infoMenu);
    }
}
