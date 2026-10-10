package raf.graffito.dsw.gui.swing.views.editor;

import raf.graffito.dsw.controller.ActionManager;

import javax.swing.*;
import java.awt.*;

public class ProjectBottomPanel extends JPanel {

    private final JButton undoButton;
    private final JButton redoButton;

    public ProjectBottomPanel(ActionManager am) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        undoButton = new JButton(am.getUndoAction());
        redoButton = new JButton(am.getRedoAction());

        // ako Action nema NAME, ostavi:
        undoButton.setText("Undo");
        redoButton.setText("Redo");

        // compact look
        undoButton.setMargin(new Insets(2, 6, 2, 6));
        redoButton.setMargin(new Insets(2, 6, 2, 6));

        add(undoButton);
        add(redoButton);
    }

    public void updateEnabled(boolean canUndo, boolean canRedo) {
        undoButton.setEnabled(canUndo);
        redoButton.setEnabled(canRedo);
    }
}
