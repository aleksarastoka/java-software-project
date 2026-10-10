package raf.graffito.dsw.core.model.factory.slideElement;

import raf.graffito.dsw.core.model.structures.SlideElement;

import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

public class SlideElementFactory {

    private final Map<SlideElementType, AbstractSlideElementFactory> factories =
            new EnumMap<>(SlideElementType.class);

    public SlideElementFactory() {
        factories.put(SlideElementType.TEXT_BOX, new TextBoxElementFactory());
        factories.put(SlideElementType.LOGO, new LogoElementFactory());
        factories.put(SlideElementType.IMAGE, new ImageElementFactory());
    }

    public SlideElement create(SlideElementType type, Point pos, Dimension size, int rotateRatio, Object payload) {
        AbstractSlideElementFactory f = factories.get(type);
        if (f == null) throw new IllegalArgumentException("No factory registered for: " + type);
        return f.create(pos, size, rotateRatio, payload);
    }
}
