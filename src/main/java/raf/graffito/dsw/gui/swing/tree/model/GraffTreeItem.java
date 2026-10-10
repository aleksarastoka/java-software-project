package raf.graffito.dsw.gui.swing.tree.model;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;

import javax.swing.tree.DefaultMutableTreeNode;


@Getter @Setter
public class GraffTreeItem extends DefaultMutableTreeNode {

    private GraffNode graffNode;


    public GraffTreeItem(GraffNode node) {
        this.graffNode = node;
    }

    @Override
    public String toString() {
        return this.graffNode.toString();
    }

    public void setName(String name) {
        this.graffNode.setName(name);
    }

}
