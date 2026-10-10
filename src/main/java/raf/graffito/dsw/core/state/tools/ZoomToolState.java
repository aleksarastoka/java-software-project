package raf.graffito.dsw.core.state.tools;

import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.state.interfaces.WheelToolState;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;
import raf.graffito.dsw.gui.swing.views.editor.SlideView;

import java.awt.*;
import java.awt.event.MouseWheelEvent;

public class ZoomToolState implements WheelToolState {
    @Override
    public String getName() {
        return "";
    }

    @Override
    public void mouseWheelMoved(Slide slide, Point p, MouseWheelEvent e) {
        SlideView sv = MainFrame.getInstance()
                .getProjectView()
                .getTabs()
                .getActiveSlideView();

        if (sv == null) return;

        int rotation = e.getWheelRotation();
        double z = sv.getZoom();
        double factor = SlideView.getZOOM_FACTOR();

        if (rotation < 0 && z < SlideView.getMAX_ZOOM()) {
            sv.setZoom(z * factor);
            sv.repaint();
        } else if (rotation > 0 && z > SlideView.getMIN_ZOOM()) {
            sv.setZoom(z / factor);
            sv.repaint();
        }
    }
}
