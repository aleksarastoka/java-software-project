// raf/graffito/dsw/core/model/factory/GraffNodeFactory.java
package raf.graffito.dsw.core.model.factory.graffNode;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Presentation;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.model.repository.implementation.Workspace;

public class GraffNodeFactory {

    private static final ProjectFactory projectFactory = new ProjectFactory();
    private static final PresentationFactory presentationFactory = new PresentationFactory();
    private static final SlideFactory slideFactory = new SlideFactory();

    public GraffNode createNode(GraffNode parent) {
        if (parent instanceof Workspace) return projectFactory.createGraffNode(parent);
        if (parent instanceof Project)   return presentationFactory.createGraffNode(parent);
        if (parent instanceof Presentation) return slideFactory.createGraffNode(parent);
        throw new IllegalArgumentException("Cannot add a child to: " + parent.getClass().getSimpleName());
    }


    public GraffNode createNode(GraffNode parent, ChildType type) {
        if (parent instanceof Project) {
            if (type == ChildType.SLIDE)        return slideFactory.createGraffNode(parent);
            if (type == ChildType.PRESENTATION) return presentationFactory.createGraffNode(parent);
            throw new IllegalArgumentException("Unknown child type for Project: " + type);
        }

        return createNode(parent);
    }
}
