package raf.graffito.dsw.gui.swing.dialogs;

import lombok.Getter;
import raf.graffito.dsw.core.model.decorator.ColorManager;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChooseColorDialog extends JDialog {

    @Getter
    private Color selectedColor = null;
    private JLabel selectedLabel;

    public ChooseColorDialog(Window parent, GraffNode projectCore) {
        super(parent, "Choose a color", ModalityType.APPLICATION_MODAL);

        setLayout(new BorderLayout(10, 10));
        setSize(450, 260);
        setLocationRelativeTo(parent);

        selectedLabel = new JLabel("Currently selected: none");
        selectedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(selectedLabel, BorderLayout.NORTH);

        JPanel colorsPanel = new JPanel(new GridLayout(0, 5, 10, 10));

        List<Color> freeColors = ColorManager.getAvailableColors(projectCore);

        for (final Color c : freeColors) {
            JButton btn = new JButton();
            btn.setBackground(c);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setPreferredSize(new Dimension(40, 40));

            btn.addActionListener(e -> {
                selectedColor = c;
                updateSelectedLabel();
                highlightSelection(colorsPanel, btn);
            });

            colorsPanel.add(btn);
        }

        if (freeColors.isEmpty()) {
            colorsPanel.add(new JLabel("Nema dostupnih boja", SwingConstants.CENTER));
        }

        add(colorsPanel, BorderLayout.CENTER);

        JPanel bottom = getJPanel();

        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel getJPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton confirm = new JButton("Confirm");
        JButton cancel = new JButton("Cancel");

        confirm.addActionListener(e -> {
            if (selectedColor == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "You must choose a color!",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            dispose();
        });

        cancel.addActionListener(e -> {
            selectedColor = null;
            dispose();
        });

        bottom.add(cancel);
        bottom.add(confirm);
        return bottom;
    }

    private void updateSelectedLabel() {
        if (selectedColor == null) {
            selectedLabel.setText("Currently selected: none");
        } else {
            selectedLabel.setText("Currently selected: " + toColorName(selectedColor));
        }
    }

    private String toColorName(Color c) {
        if (c.equals(Color.RED)) return "Red";
        if (c.equals(Color.GREEN)) return "Green";
        if (c.equals(Color.BLUE)) return "Blue";
        if (c.equals(Color.ORANGE)) return "Orange";
        if (c.equals(Color.YELLOW)) return "Yellow";
        if (c.equals(Color.PINK)) return "Pink";
        if (c.equals(Color.GRAY)) return "Gray";

        if (c.equals(new Color(128, 0, 128))) return "Purple";
        if (c.equals(new Color(135, 50, 50))) return "Brown";
        if (c.equals(new Color(0, 255, 255))) return "Cyan";

        return "RGB(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ")";
    }

    private void highlightSelection(JPanel parent, JButton selectedBtn) {
        Component[] comps = parent.getComponents();
        for (Component comp : comps) {
            if (comp instanceof JButton b) {
                if (b == selectedBtn) {
                    b.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
                } else {
                    b.setBorder(BorderFactory.createEmptyBorder());
                }
            }
        }
    }

}
