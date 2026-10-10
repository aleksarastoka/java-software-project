package raf.graffito.dsw.core.command.slide;

import raf.graffito.dsw.core.command.Command;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;

import java.awt.*;

public class ResizeElementCommand implements Command {

    private final Slide slide;
    private final SlideElement element;

    private final Point beforePos;
    private final Dimension beforeSize;

    private final Point afterPos;
    private final Dimension afterSize;

    public ResizeElementCommand(Slide slide,
                                SlideElement element,
                                Point beforePos, Dimension beforeSize,
                                Point afterPos, Dimension afterSize) {
        this.slide = slide;
        this.element = element;

        this.beforePos = new Point(beforePos);
        this.beforeSize = new Dimension(beforeSize);

        this.afterPos = new Point(afterPos);
        this.afterSize = new Dimension(afterSize);
    }

    @Override
    public void execute() {
        apply(afterPos, afterSize);
    }

    @Override
    public void undo() {
        apply(beforePos, beforeSize);
    }

    @Override
    public String name() {
        return "Resize Element";
    }

    private void apply(Point pos, Dimension size) {
        element.getPos().x = pos.x;
        element.getPos().y = pos.y;
        element.getSize().width = size.width;
        element.getSize().height = size.height;

        // ensure UI updates on undo/redo
        slide.notifySubscribers(slide);
    }
}
