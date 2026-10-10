package raf.graffito.dsw.core.model.factory.graffNode;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;

public abstract class AbstractGraffNodeFactory {
    public abstract GraffNode createGraffNode(GraffNode node);
}
