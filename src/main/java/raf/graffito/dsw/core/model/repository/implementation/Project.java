package raf.graffito.dsw.core.model.repository.implementation;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite;

import static raf.graffito.dsw.core.model.decorator.Decorators.unwrap;

@Getter @Setter
public class Project extends GraffNodeComposite {
    private String title;
    private String author;
    private int number = 0;

    public Project(GraffNode parent, String title, String author, String name) {
        super(name, parent);
        this.title = title;
        this.author = author;
        if (parent instanceof GraffNodeComposite comp) comp.addChild(this);
    }

    @Override
    public void addChild(GraffNode child) {
        if (child == null) throw new IllegalArgumentException("Child cannot be null.");
        if (!(unwrap(child) instanceof Presentation) && !(unwrap(child) instanceof Slide))
            throw new IllegalArgumentException("Project can only contain Presentation nodes.");
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


    @Override
    public String toString() {
        return this.getName();
    }
}

