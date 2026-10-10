package raf.graffito.dsw.core.model.decorator;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.gui.swing.color.ColorTags;

import java.awt.*;

@Getter @Setter
public class ColorDecorator extends GraffNode {
    private GraffNode decoratedNode;
    private Color color;

    public ColorDecorator(GraffNode decoratedNode, Color color) {
        this.decoratedNode = decoratedNode;
        this.color = color;

        // mirror basic fields if you were doing that
        setName(decoratedNode.getName());
        setParent(decoratedNode.getParent());

        // register tag for the core node
        ColorTags.set(decoratedNode, color);
    }

    public void setColor(Color c) {
        this.color = c;
        ColorTags.set(decoratedNode, c); // keep registry in sync
    }

}
