package raf.graffito.dsw.core.command.slide;

import raf.graffito.dsw.core.command.Command;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;

public class RotateElementCommand implements Command {

    private final Slide slide;
    private final SlideElement element;
    private final int before;
    private final int after;

    public RotateElementCommand(Slide slide, SlideElement element, int before, int after) {
        this.slide = slide;
        this.element = element;
        this.before = normalize(before);
        this.after  = normalize(after);
    }

    @Override
    public void execute() {
        element.setRotateRatio(after);
        slide.notifySubscribers(slide);
    }

    @Override
    public void undo() {
        element.setRotateRatio(before);
        slide.notifySubscribers(slide);
    }

    @Override
    public String name() {
        return "Rotate Element";
    }

    private static int normalize(int r) {
        r %= 360;
        if (r < 0) r += 360;
        return r;
    }
}
