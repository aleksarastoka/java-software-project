package raf.graffito.dsw.gui.swing.views.dialogs;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class AboutUsFrame extends JFrame {
    private JPanel mainPanel;

    public AboutUsFrame() {
        initComponents();
        setTitle("About Us");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        JPanel person1Panel = createPersonPanel("Lazar Marinkovic", "81/24RN", "/images/lmarinkovic.jpg");
        JPanel person2Panel = createPersonPanel("Aleksa Rastoka", "91/25RN", "/images/aRastoka.png");

        mainPanel.add(person1Panel);
        mainPanel.add(person2Panel);
        add(mainPanel);
    }

    private JPanel createPersonPanel(String name, String id, String imagePath) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel("Ime: " + name, SwingConstants.CENTER);
        JLabel idLabel = new JLabel("Indeks: " + id, SwingConstants.CENTER);
        JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        ImageIcon icon = loadImage(imagePath, 180, 180);
        if (icon != null) imageLabel.setIcon(icon); else imageLabel.setText("Image not found");

        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(10));
        panel.add(nameLabel);
        panel.add(idLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(imageLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private ImageIcon loadImage(String resourcePath, int width, int height) {
        URL url = getClass().getResource(resourcePath);
        if (url == null) {
            System.err.println("Image not found: " + resourcePath);
            return null;
        }
        Image img = new ImageIcon(url).getImage();
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
