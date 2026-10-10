package raf.graffito.dsw.core.model.decorator;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Project;

public final class NodeUtils {

    private NodeUtils() {}

    public static GraffNode core(GraffNode node) {
        while (node instanceof ColorDecorator) {
            node = ((ColorDecorator) node).getDecoratedNode();
        }
        return node;
    }

    public static GraffNode owningProjectCore(GraffNode node) {
        GraffNode current = core(node);

        while (current != null && !(current instanceof Project)) {
            GraffNode parent = current.getParent();
            if (parent == null)
                return null;
            current = core(parent);
        }

        return current;
    }
}
