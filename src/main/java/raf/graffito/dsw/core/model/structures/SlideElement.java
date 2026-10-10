package raf.graffito.dsw.core.model.structures;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;

import java.awt.*;

@Setter
@Getter
public abstract class SlideElement extends GraffNode implements Prototype {
    /// apstraktni model elementa koji zivi na slajdu i zna kako da se nacrta
    /// -> sta se nalazi na papiru
    protected Point pos;
    protected Dimension size;
    protected int rotateRatio;

    public SlideElement(String name, GraffNode parent, Point pos, Dimension size) {
        super(parent, name);
        this.pos = pos;
        this.size = size;
    }
    public SlideElement(Point pos, Dimension size, int rotateRatio) {
        this.pos = pos;
        this.size = size;
        this.rotateRatio = rotateRatio;
    }

    public abstract void paint(Graphics2D g);

    @Override
    public abstract Prototype clone();
}
