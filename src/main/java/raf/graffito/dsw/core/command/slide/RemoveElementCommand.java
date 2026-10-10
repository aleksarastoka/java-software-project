package raf.graffito.dsw.core.command.slide;

import raf.graffito.dsw.core.command.Command;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;

public class RemoveElementCommand implements Command {

    private final Slide slide;
    private final SlideElement element;

    // to restore in the same position on undo
    private int formerIndex = -1;

    public RemoveElementCommand(Slide slide, SlideElement element) {
        this.slide = slide;
        this.element = element;
    }

    @Override
    public void execute() {
        formerIndex = slide.getChildren().indexOf(element);
        slide.removeChild(element);
    }

    @Override
    public void undo() {
        // restore at original position if possible
        if (formerIndex < 0) {
            slide.addChild(element);
        } else {
            slide.addElementAt(element, formerIndex);
        }
    }

    @Override
    public String name() {
        return "Remove Element";
    }
}
