package raf.graffito.dsw.core.model.factory.slideElement;

import raf.graffito.dsw.core.model.structures.LogoElement;
import raf.graffito.dsw.core.model.structures.SlideElement;

import java.awt.*;

public class LogoElementFactory extends AbstractSlideElementFactory {
    private static int counter = 1;

    @Override
    public SlideElement create(Point pos, Dimension size, int rotateRatio, Object payload) {
        String name = "Logo" + counter++;
        return new LogoElement(name, pos, size, rotateRatio);
    }
}
