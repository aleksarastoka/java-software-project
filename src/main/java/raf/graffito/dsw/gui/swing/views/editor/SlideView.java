package raf.graffito.dsw.gui.swing.views.editor;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.controller.mediator.Colleague;
import raf.graffito.dsw.controller.mediator.ScreenMode;
import raf.graffito.dsw.core.message.ISubscriber;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.gui.swing.painter.SlidePainter;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import java.awt.*;

public class SlideView extends JPanel implements ISubscriber<Slide>, Colleague {

    public static final int SLIDE_W = 960;
    public static final int SLIDE_H = 540;

    public static final int OUTER_PADDING = 20;

    @Getter @Setter
    private double zoom = 1.0;

    @Getter
    private static final double MAX_ZOOM = 3.0;
    @Getter
    private static final double MIN_ZOOM = 0.8;
    @Getter
    private static final double ZOOM_FACTOR = 1.1;

    @Getter
    private final Slide slide;

    private final SlidePainter painter = new SlidePainter();
    private final SlideCanvas canvas;

    private boolean autoFitZoom = true;

    @Getter
    private boolean editingLocked = false;

    public SlideView(Slide slide) {
        this.slide = slide;

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(60, 60, 60));

        if (this.slide != null) this.slide.addSubscriber(this);

        this.canvas = new SlideCanvas(
                this.slide,
                painter,
                () -> this.zoom,
                () -> this.autoFitZoom,
                this::requestFit,
                locked -> {
                    this.editingLocked = locked;
                    MainFrame.getInstance().getProjectView().setEditingLock(locked);
                }
        );

        add(canvas, BorderLayout.CENTER);

        requestFit();
    }

    public void onScreenSizeChanged() {
        autoFitZoom = true;
        requestFit();
    }

    @Override
    public void onScreenModeChanged(ScreenMode mode, double uiScale) {
        onScreenSizeChanged();
    }

    @Override
    public void update(Slide slide) {
        MainFrame.getInstance().getProjectView().updateUndoRedoState();
        MainFrame.getInstance().getProjectView().refreshActiveSlideHeader();
        repaint();
    }

    public void setZoom(double zoom) {
        this.zoom = Math.max(MIN_ZOOM, Math.min(zoom, MAX_ZOOM));
        autoFitZoom = false;
        canvas.repaint();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (slide != null) slide.removeSubscriber(this);
    }

    private void requestFit() {
        SwingUtilities.invokeLater(this::fitZoomToViewport);
    }

    private void fitZoomToViewport() {
        int panelW = canvas.getWidth();
        int panelH = canvas.getHeight();
        if (panelW <= 0 || panelH <= 0) return;

        int availW = Math.max(1, panelW - 2 * OUTER_PADDING);
        int availH = Math.max(1, panelH - 2 * OUTER_PADDING);

        double zx = availW / (double) SLIDE_W;
        double zy = availH / (double) SLIDE_H;

        double fit = Math.min(zx, zy);

        zoom = Math.max(0.05, Math.min(fit, MAX_ZOOM));
        canvas.repaint();
    }
}
