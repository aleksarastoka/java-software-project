package raf.graffito.dsw.gui.swing.views.mediator;

import raf.graffito.dsw.controller.mediator.Colleague;
import raf.graffito.dsw.controller.mediator.ScreenMode;
import raf.graffito.dsw.controller.mediator.ScreenSizeMediator;

import javax.swing.*;
import java.awt.*;

public class ScreenSizeMediatorPanel extends JPanel implements Colleague {

    private final JRadioButton normalBtn = new JRadioButton("Normal");
    private final JRadioButton fullscreenBtn = new JRadioButton("Fullscreen");
    private final JRadioButton smallBtn = new JRadioButton("Small");

    private boolean updating = false;

    public ScreenSizeMediatorPanel(ScreenSizeMediator mediator) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));

        ButtonGroup group = new ButtonGroup();
        group.add(normalBtn);
        group.add(fullscreenBtn);
        group.add(smallBtn);

        // initial selection
        setSelectedMode(mediator.getMode());

        normalBtn.addActionListener(e -> {
            if (updating) return;
            mediator.setMode(ScreenMode.NORMAL);
        });

        fullscreenBtn.addActionListener(e -> {
            if (updating) return;
            mediator.setMode(ScreenMode.FULLSCREEN);
        });

        smallBtn.addActionListener(e -> {
            if (updating) return;
            mediator.setMode(ScreenMode.SMALL);
        });

        add(new JLabel("Screen size:"));
        add(normalBtn);
        add(fullscreenBtn);
        add(smallBtn);
    }

    public void setSelectedMode(ScreenMode mode) {
        if (mode == null) return;

        updating = true;
        try {
            normalBtn.setSelected(mode == ScreenMode.NORMAL);
            fullscreenBtn.setSelected(mode == ScreenMode.FULLSCREEN);
            smallBtn.setSelected(mode == ScreenMode.SMALL);
        } finally {
            updating = false;
        }
    }

    @Override
    public void onScreenModeChanged(ScreenMode mode, double uiScale) {
        setSelectedMode(mode);
        revalidate();
        repaint();
    }
}
