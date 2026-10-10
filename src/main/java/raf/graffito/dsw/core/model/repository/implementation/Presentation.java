package raf.graffito.dsw.core.model.repository.implementation;

import lombok.Getter;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite;

import static raf.graffito.dsw.core.model.decorator.Decorators.unwrap;

@Getter
public class Presentation extends GraffNodeComposite {

    private final int slideWidth  = 1280;
    private final int slideHeight = 720;

    public Presentation(GraffNode parent, String name) {
        super(name, parent);

        if (parent instanceof GraffNodeComposite comp) {
            comp.addChild(this);
        }
    }
    @Override
    public void addChild(GraffNode child) {
        if (child == null)
            throw new IllegalArgumentException("Child cannot be null.");

        // Allow only Slide under Presentation
        if (!(unwrap(child) instanceof Slide))
            throw new IllegalArgumentException("Presentation can only contain Slide nodes.");

        if (!this.getChildren().contains(child))
            super.addChild(child);
    }




    public void moveSlide(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) return;
        var list = getChildren(); // from GraffNodeComposite
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= list.size() || toIndex > list.size())
            throw new IndexOutOfBoundsException();
        var slide = list.remove(fromIndex);
        // when moving down, removing shifts indices left; clamp target
        if (toIndex > list.size()) toIndex = list.size();
        list.add(toIndex, slide);
    }

    /* ako ce birati dimenzije
    public void setSlideSize(int width, int height) {
        if (width <= 0 || height <= 0) return;  // ili baciš exception / message
        this.slideWidth = width;
        this.slideHeight = height;
        // eventualno kasnije: notifySubscribers da svi SlideView-ovi repaintuju
    }
     */

    @Override
    public String toString() {
        return this.getName();
    }
}

