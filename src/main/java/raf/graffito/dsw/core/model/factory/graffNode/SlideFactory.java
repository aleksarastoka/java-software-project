package raf.graffito.dsw.core.model.factory.graffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Slide;

public class SlideFactory extends AbstractGraffNodeFactory {
    private static int number = 1;

    @Override
    public GraffNode createGraffNode(GraffNode node) {
        String name = "Slide"+number++;
        return new Slide(name, node);
    }
}
