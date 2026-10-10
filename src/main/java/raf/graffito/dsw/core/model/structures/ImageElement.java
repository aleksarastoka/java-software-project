package raf.graffito.dsw.core.model.structures;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
@Getter
@Setter
public class ImageElement extends SlideElement {
    ///  konkretan SlideElement koji crta sliku na slajd
    /// -> konkretna slika na papiru
    private BufferedImage image;
    private static int amountOfCopies = 0;

    public ImageElement(String name, Point pos, Dimension size, int rotateRatio, BufferedImage image) {
        super(pos, size, rotateRatio);
        this.image = image;
        super.setName(name);
    }

    @Override
    public void paint(Graphics2D g) {
        if (image == null) return;

        AffineTransform old = g.getTransform();

        int cx = pos.x + size.width / 2;
        int cy = pos.y + size.height / 2;

        g.rotate(Math.toRadians(rotateRatio), cx, cy);
        g.drawImage(image, pos.x, pos.y, size.width, size.height, null);

        g.setTransform(old);
    }

    @Override
    public Prototype clone() {
        String baseName = getName() + ".copy";

        String cloneName = (amountOfCopies == 0)
                ? baseName
                : baseName + amountOfCopies;

        ImageElement e = new ImageElement(
                cloneName,
                getPos(),
                getSize(),
                getRotateRatio(),
                getImage()
        );

        amountOfCopies++;
        return e;
    }

}
