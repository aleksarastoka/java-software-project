package raf.graffito.dsw.core.model.repository.implementation;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite;

import static raf.graffito.dsw.core.model.decorator.Decorators.unwrap;

@Getter @Setter
public class Workspace extends GraffNodeComposite {

    public Workspace(String name) {
        super(name, null);
    }

    @Override
    public void addChild(GraffNode child) {
        if (child == null) throw new IllegalArgumentException("Child cannot be null.");
        if (!(unwrap(child) instanceof Project))
            throw new IllegalArgumentException("Workspace can only contain Project nodes.");
        if (!this.getChildren().contains(child))
            super.addChild(child);
    }


    @Override
    public String toString() {
        return this.getName();
    }
}
