package raf.graffito.dsw.gui.swing.views.sidebar;

import raf.graffito.dsw.controller.ActionManager;
import raf.graffito.dsw.core.state.ToolType;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;

public class ToolsToolBar extends JPanel {

    private static final int BTN_W = 34;
    private static final int BTN_H = 34;

    private final JButton loadImagesButton = new JButton();

    private final ButtonGroup group = new ButtonGroup();
    private final EnumMap<ToolType, JToggleButton> buttons = new EnumMap<>(ToolType.class);

    public ToolsToolBar(ActionManager am) {
        super(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Top: Load Images button (normal JButton)
        loadImagesButton.setAction(am.getLoadImagesAction());
        loadImagesButton.setText(null);
        loadImagesButton.setToolTipText((String) am.getLoadImagesAction().getValue(Action.SHORT_DESCRIPTION));
        loadImagesButton.setFocusPainted(false);
        loadImagesButton.setMargin(new Insets(0, 0, 0, 0));
        loadImagesButton.setPreferredSize(new Dimension(2 * BTN_W + 4, BTN_H)); // širina kao 2 dugmeta
        loadImagesButton.setEnabled(true);
        add(loadImagesButton, BorderLayout.NORTH);

        // Center: grid of toggle tools
        JPanel grid = new JPanel(new GridLayout(0, 2, 4, 4));
        grid.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        addToggle(grid, am.getSelectToolAction(), ToolType.SELECT);
        addToggle(grid, am.getMoveToolAction(), ToolType.MOVE);
        addToggle(grid, am.getResizeToolAction(), ToolType.RESIZE);
        addToggle(grid, am.getRotateToolAction(), ToolType.ROTATE);
        addToggle(grid, am.getAddTextToolAction(), ToolType.ADD_TEXT);
        addToggle(grid, am.getAddLogoToolAction(), ToolType.ADD_LOGO);
        addToggle(grid, am.getAddImageToolAction(), ToolType.ADD_IMAGE);
        addToggle(grid, am.getDeleteToolAction(), ToolType.DELETE);
        addToggle(grid, am.getCloneToolAction(), ToolType.CLONE);

        add(grid, BorderLayout.CENTER);

        setPressed(ToolType.SELECT);
        setToolsEnabled(false);
    }

    private void addToggle(JPanel parent, Action action, ToolType tool) {
        JToggleButton b = new JToggleButton(action);
        b.setText(null);
        b.setToolTipText((String) action.getValue(Action.NAME));
        b.setFocusPainted(false);

        b.setMargin(new Insets(0, 0, 0, 0));
        b.setPreferredSize(new Dimension(BTN_W, BTN_H));
        b.setMinimumSize(new Dimension(BTN_W, BTN_H));
        b.setMaximumSize(new Dimension(BTN_W, BTN_H));

        group.add(b);
        buttons.put(tool, b);

        b.addActionListener(e -> setPressed(tool));
        parent.add(b);
    }

    private void setPressed(ToolType tool) {
        JToggleButton b = buttons.get(tool);
        if (b != null) b.setSelected(true);
    }

    public void setToolsEnabled(boolean enabled) {
        loadImagesButton.setEnabled(true);      // ✅ load images disabled if no slide selected (po želji)
        for (JToggleButton b : buttons.values()) b.setEnabled(enabled);
        repaint();
    }
}
