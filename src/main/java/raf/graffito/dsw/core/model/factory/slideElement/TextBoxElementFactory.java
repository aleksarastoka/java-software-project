package raf.graffito.dsw.core.model.factory.slideElement;

import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.model.structures.TextBoxElement;

import java.awt.*;

public class TextBoxElementFactory extends AbstractSlideElementFactory {
    private static int counter = 1;
    @Override
    public SlideElement create(Point pos, Dimension size, int rotateRatio, Object payload) {
        String name =  "TextBox" + counter++;
        return new TextBoxElement(name, pos, size, rotateRatio);
    }
}
