package raf.graffito.dsw.gui.swing.dialogs;

import javax.swing.*;
import java.awt.*;

public class ProjectEditDialog extends JDialog {
    private final JTextField titleField = new JTextField(24);
    private final JTextField authorField = new JTextField(24);
    private Result result; // null if canceled

    public static class Result {
        public final String title;
        public final String author;
        public Result(String title, String author) { this.title = title; this.author = author; }
    }

    public ProjectEditDialog(Window owner, String currentTitle, String currentAuthor) {
        super(owner, "Edit Project", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        titleField.setText(currentTitle != null ? currentTitle : "");
        authorField.setText(currentAuthor != null ? currentAuthor : "");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; form.add(new JLabel("Title:"), gc);
        gc.gridx = 1; gc.weightx = 1; form.add(titleField, gc);
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; form.add(new JLabel("Author:"), gc);
        gc.gridx = 1; gc.weightx = 1; form.add(authorField, gc);

        JButton cancel = new JButton("Cancel");
        JButton save = new JButton("Save");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel); buttons.add(save);

        cancel.addActionListener(e -> { result = null; dispose(); });
        save.addActionListener(e -> {
            result = new Result(titleField.getText(), authorField.getText());
            dispose();
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    public Result showDialog() {
        setVisible(true); // blocks on EDT
        return result;
    }
}
