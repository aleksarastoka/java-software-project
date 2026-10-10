// gui/swing/dialogs/ChooseChildTypeDialog.java
package raf.graffito.dsw.gui.swing.dialogs;

import javax.swing.*;
import java.awt.*;

public class ChooseChildTypeDialog extends JDialog {
    public enum Result { PRESENTATION, SLIDE, CANCEL }

    private Result result = Result.CANCEL;

    public ChooseChildTypeDialog(Window owner) {
        super(owner, "Create under Project", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        var label = new JLabel("What would you like to add under this Project?");
        var presentationBtn = new JButton("Presentation");
        var slideBtn = new JButton("Slide");
        var cancelBtn = new JButton("Cancel");

        presentationBtn.addActionListener(e -> { result = Result.PRESENTATION; dispose(); });
        slideBtn.addActionListener(e -> { result = Result.SLIDE; dispose(); });
        cancelBtn.addActionListener(e -> { result = Result.CANCEL; dispose(); });

        var buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(presentationBtn);
        buttons.add(slideBtn);
        buttons.add(cancelBtn);

        var panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        panel.add(label, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
    }

    public Result showDialog() {
        setVisible(true);
        return result;
    }
}
