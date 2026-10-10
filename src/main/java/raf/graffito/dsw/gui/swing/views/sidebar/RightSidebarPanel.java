package raf.graffito.dsw.gui.swing.views.sidebar;

import lombok.Getter;
import raf.graffito.dsw.controller.ActionManager;

import javax.swing.*;
import java.awt.*;

public class RightSidebarPanel extends JPanel {

    @Getter
    private final ImagePalettePanel imagePalettePanel;

    private final ToolsToolBar toolsToolBar;

    public RightSidebarPanel(ActionManager am) {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setPreferredSize(new Dimension(180, 0)); // fixed width

        imagePalettePanel = new ImagePalettePanel();
        toolsToolBar = new ToolsToolBar(am);

        add(imagePalettePanel, BorderLayout.CENTER);
        add(toolsToolBar, BorderLayout.SOUTH);

        setEnabledForSlide(false);
    }

    public void setEnabledForSlide(boolean enabled) {
        imagePalettePanel.setActive(enabled);
        toolsToolBar.setToolsEnabled(enabled);
    }
}
