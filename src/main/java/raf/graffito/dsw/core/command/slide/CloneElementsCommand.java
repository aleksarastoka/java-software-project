package raf.graffito.dsw.core.command.slide;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.command.Command;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import java.util.ArrayList;
import java.util.List;

public class CloneElementsCommand implements Command {

    private final Slide slide;
    private final List<SlideElement> clones;

    public CloneElementsCommand(Slide slide, List<SlideElement> clones) {
        this.slide = slide;
        this.clones = new ArrayList<>(clones);
    }

    @Override
    public void execute() {
        for (SlideElement el : clones) {
            // avoid duplicates if execute called multiple times accidentally
            if (!slide.getChildren().contains(el)) {
                slide.addChild(el);
                try {
                    MainFrame.getInstance().getGraffTree().mirrorModelNodeAdded(slide, el);
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void undo() {
        for (SlideElement el : clones) {
            ApplicationFramework.getInstance().getToolStateManager().removeFromSelection(el);
            slide.removeChild(el);
        }
    }

    @Override
    public String name() {
        return "Clone Elements";
    }

    public List<SlideElement> getClones() {
        return clones;
    }
}
