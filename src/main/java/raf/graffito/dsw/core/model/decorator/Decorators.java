package raf.graffito.dsw.core.model.decorator;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;

public final class Decorators {
    private Decorators() {}

    public static GraffNode unwrap(GraffNode node) {
        while (node instanceof ColorDecorator cd) node = cd.getDecoratedNode();
        return node;
    }
}
