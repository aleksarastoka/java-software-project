package raf.graffito.dsw.core.command.slide;

import raf.graffito.dsw.core.command.Command;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;

import java.awt.*;

public class MoveElementCommand implements Command {

    private final Slide slide;
    private final SlideElement element;
    private final Point before;
    private final Point after;

    public MoveElementCommand(Slide slide, SlideElement element, Point before, Point after) {
        this.slide = slide;
        this.element = element;
        this.before = new Point(before);
        this.after = new Point(after);
    }

    @Override
    public void execute() {
        apply(after);
    }

    @Override
    public void undo() {
        apply(before);
    }

    private void apply(Point p) {
        element.getPos().x = p.x;
        element.getPos().y = p.y;

        // ensure repaint on undo/redo
        slide.notifySubscribers(slide);
    }

    @Override
    public String name() {
        return "Move Element";
    }
}
