package raf.graffito.dsw.core.state.interfaces;

import raf.graffito.dsw.core.model.repository.implementation.Slide;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface ToolState {

    String getName();

    default void mousePressed(Slide slide, Point slidePoint, MouseEvent e) {}
    default void mouseDragged(Slide slide, Point slidePoint, MouseEvent e) {}
    default void mouseReleased(Slide slide, Point slidePoint, MouseEvent e) {}
    default void mouseClicked(Slide slide, Point slidePoint, MouseEvent e) {}
}
