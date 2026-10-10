package raf.graffito.dsw.core.model.repository.composite;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;

@Getter @Setter
public abstract class GraffNodeComposite extends GraffNode {
    private ArrayList<GraffNode> children;

    public GraffNodeComposite(String name, GraffNode parent) {
        super(parent, name);
        this.children = new ArrayList<>();
    }

    public GraffNodeComposite(GraffNode parent, String name, ArrayList<GraffNode> children) {
        super(parent, name);
        this.children = children;
    }

    public void addChild(final GraffNode child) {
        if (child == null) return;

        if (children == null) {
            children = new ArrayList<>();
        }

        // ✅ ensure model parent pointer is correct
        child.setParent(this);

        children.add(child);
    }

    public void removeChild(final GraffNode child) {
        if (children == null || child == null) return;

        boolean removed = children.remove(child);

        // ✅ clear parent pointer if we actually removed it
        if (removed) {
            child.setParent(null);
        }
    }


    @Override
    public GraffNode findByName(String name) {
        GraffNode self = super.findByName(name);
        if (self != null) return self;

        if (children != null) {
            for (GraffNode child : children) {
                GraffNode hit = child.findByName(name);
                if (hit != null) return hit;
            }
        }
        return null;
    }
}
