package raf.graffito.dsw.core.model.factory.graffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Presentation;


public class PresentationFactory extends AbstractGraffNodeFactory {
    private static int number = 1;

    @Override
    public GraffNode createGraffNode(GraffNode node) {
        String name = "Presentation"+number++;
        return new Presentation(node, name);
    }
}
