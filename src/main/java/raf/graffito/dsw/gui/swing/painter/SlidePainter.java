package raf.graffito.dsw.gui.swing.painter;

import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.gui.swing.views.editor.SlideView;

import java.awt.*;

public class SlidePainter {

    /// Klasa za handlovanje crtanja slida

    private SlideElement element;
    public void paint(Graphics2D g2, Slide slide, double zoom, int panelW, int panelH) {
        if (g2 == null) return;

        // Canvas background (around the slide)
        g2.setColor(new Color(60, 60, 60));
        g2.fillRect(0, 0, panelW, panelH);

        // Slide "paper" area (white)
        int slideW = (int) Math.round(SlideView.SLIDE_W * zoom);
        int slideH = (int) Math.round(SlideView.SLIDE_H * zoom);

        int x0 = (panelW - slideW) / 2;
        int y0 = (panelH - slideH) / 2;

        g2.setColor(Color.WHITE);
        g2.fillRect(x0, y0, slideW, slideH);

        // subtle border
        g2.setColor(new Color(200, 200, 200));
        g2.drawRect(x0, y0, slideW, slideH);

        if (slide == null) return;

        // Draw elements in slide coordinate system
        Graphics2D gSlide = (Graphics2D) g2.create();
        try {
            gSlide.translate(x0, y0);
            gSlide.scale(zoom, zoom);

            if (slide.getChildren() != null) {
                for (var node : slide.getChildren()) {
                    if (node instanceof SlideElement el) {
                        el.paint(gSlide);
                    }
                }
            }
        } finally {
            gSlide.dispose();
        }
    }
}
