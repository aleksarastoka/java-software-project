package raf.graffito.dsw.core.model.factory.slideElement;

import raf.graffito.dsw.core.model.structures.ImageElement;
import raf.graffito.dsw.core.model.structures.SlideElement;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageElementFactory extends AbstractSlideElementFactory {

    private static int counter = 1;

    @Override
    public SlideElement create(Point pos, Dimension size, int rotateRatio, Object payload) {
        BufferedImage img = (BufferedImage) payload;
        String name = "Image" + counter++;
        return new ImageElement(name, pos, size, rotateRatio, img);
    }
}
