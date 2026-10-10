package raf.graffito.dsw.core.model.factory.graffNode;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Project;

public class ProjectFactory extends AbstractGraffNodeFactory {
    private static int number = 1;

    @Override
    public GraffNode createGraffNode(GraffNode node) {
        String name = "Project"+number++;
        return new Project(node, "Project 1", "Name", name);
    }
}
