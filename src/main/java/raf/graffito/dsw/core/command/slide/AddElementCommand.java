package raf.graffito.dsw.core.command.slide;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.command.Command;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;

public class AddElementCommand implements Command {
    private final Slide slide;
    private final SlideElement element;

    public AddElementCommand(Slide slide, SlideElement element) {
        this.slide = slide;
        this.element = element;
    }

    @Override
    public void execute() {
        slide.addChild(element);
    }

    @Override
    public void undo() {
        ApplicationFramework.getInstance().getToolStateManager().removeFromSelection(element);
        slide.removeChild(element);

    }

    @Override
    public String name() {
        return "Add Element";
    }
}
