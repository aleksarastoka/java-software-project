package raf.graffito.dsw.core.state.interfaces;

import raf.graffito.dsw.core.model.repository.implementation.Slide;

import java.awt.*;
import java.awt.event.MouseWheelEvent;

public interface WheelToolState extends  ToolState {
    default void mouseWheelMoved(Slide slide, Point p, MouseWheelEvent e) {}
}
