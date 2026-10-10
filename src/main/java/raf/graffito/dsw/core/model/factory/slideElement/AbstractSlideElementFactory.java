package raf.graffito.dsw.core.model.factory.slideElement;

import raf.graffito.dsw.core.model.structures.SlideElement;

import java.awt.*;

public abstract class AbstractSlideElementFactory {
    public abstract SlideElement create(Point pos, Dimension size, int rotateRatio, Object payload);
}
