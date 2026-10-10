package raf.graffito.dsw.core.model.repository.composite;

import lombok.Getter;
import lombok.Setter;
import java.util.Objects;

@Getter @Setter
public abstract class GraffNode {
    private GraffNode parent;
    private String name;

    public GraffNode(GraffNode parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    protected GraffNode() {
    }

    public GraffNode findByName(String name) {
        if (Objects.equals(name, this.name)) return this;
        return null; // composites will extend this to recurse
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + name + ")";
    }

}
