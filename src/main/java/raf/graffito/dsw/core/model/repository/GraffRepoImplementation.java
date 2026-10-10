// raf/graffito/dsw/core/model/repository/GraffRepoImplementation.java
package raf.graffito.dsw.core.model.repository;

import lombok.Getter;
import raf.graffito.dsw.core.model.decorator.Decorators;
import raf.graffito.dsw.core.model.factory.graffNode.ChildType;
import raf.graffito.dsw.core.model.factory.graffNode.GraffNodeFactory;
import raf.graffito.dsw.core.model.factory.slideElement.SlideElementFactory;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite;
import raf.graffito.dsw.core.model.repository.implementation.Workspace;

import java.util.ArrayList;
import java.util.Objects;

@Getter
public class GraffRepoImplementation implements GraffRepository {

    private final Workspace workspace = new Workspace("Workspace");
    private final GraffNodeFactory graffNodeFactory = new GraffNodeFactory();

    // ✅ Keep factory accessible (creation is NOT a repo responsibility,
    // but exposing a core factory as a service is fine and keeps wiring stable)
    private final SlideElementFactory slideElementFactory = new SlideElementFactory();

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public GraffNode addChild(GraffNodeComposite parent) {
        Objects.requireNonNull(parent, "Parent must not be null");
        GraffNode child = graffNodeFactory.createNode(parent);
        parent.addChild(child);
        return child;
    }

    @Override
    public GraffNode addChild(GraffNodeComposite parent, ChildType type) {
        Objects.requireNonNull(parent, "Parent must not be null");
        GraffNode child = graffNodeFactory.createNode(parent, type);
        parent.addChild(child);
        return child;
    }

    /**
     * ✅ Pure repository delete:
     * - no UI messaging
     * - no commands
     * - just removes node from its parent composite
     */
    @Override
    public void deleteChild(GraffNode child) {
        Objects.requireNonNull(child, "Child must not be null");

        GraffNode coreChild = Decorators.unwrap(child);

        GraffNode parent = coreChild.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("Cannot delete root or detached node");
        }

        GraffNode coreParent = Decorators.unwrap(parent);
        if (!(coreParent instanceof GraffNodeComposite compositeParent)) {
            throw new IllegalArgumentException("Parent is not a composite");
        }

        compositeParent.removeChild(coreChild);
        coreChild.setParent(null);
    }

    @Override
    public void attachChild(GraffNodeComposite parent, GraffNode child, int index) {
        Objects.requireNonNull(parent, "Parent must not be null");
        Objects.requireNonNull(child, "Child must not be null");

        GraffNode coreChild = Decorators.unwrap(child);

        // remove from old parent if present
        if (coreChild.getParent() instanceof GraffNodeComposite oldParent) {
            oldParent.getChildren().remove(coreChild);
        }

        coreChild.setParent(parent);

        if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());

        index = Math.max(0, Math.min(index, parent.getChildren().size()));
        parent.getChildren().add(index, coreChild);
    }
}
