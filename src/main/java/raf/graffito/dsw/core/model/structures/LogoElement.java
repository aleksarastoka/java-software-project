package raf.graffito.dsw.core.model.structures;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.factory.slideElement.SlideElementType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

@Getter
@Setter
public class  LogoElement extends SlideElement {

    // opciono: boja/stroke
    private Color color = new Color(20, 20, 20);
    private float stroke = 0.08f; // u "logo" koordinatama (skalira se)
    private static int amountOfCopies = 0;

    public LogoElement(String name, Point pos, Dimension size, int rotateRatio) {
        super(pos, size, rotateRatio);
        super.setName(name);
    }

    @Override
    public void paint(Graphics2D g) {
        AffineTransform oldTx = g.getTransform();
        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();

        try {
            // 1) Pomeri u pos i skaliraj na size (logo je u 0..1)
            g.translate(pos.x, pos.y);

            // 2) Rotacija oko centra elementa (u pikselima, nakon skaliranja)
            double cx = size.width / 2.0;
            double cy = size.height / 2.0;
            g.rotate(Math.toRadians(rotateRatio), cx, cy);

            // 3) Skaliranje: 1.0 logo koordinata == width/height u pikselima
            g.scale(size.width, size.height);

            // 4) Podesi stil crtanja u NORMALIZOVANIM koordinatama
            g.setColor(color);
            g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // 5) Nacrtaj LA (u koordinatama 0..1)
            GeneralPath p = buildLAPath();
            g.draw(p);

            // (opciono) popunjavanje nekih delova:
            // g.fill(p);

        } finally {
            g.setTransform(oldTx);
            g.setStroke(oldStroke);
            g.setColor(oldColor);
        }
    }

    @Override
    public Prototype clone() {
        String baseName = getName() + ".copy";

        String cloneName = (amountOfCopies == 0)
                ? baseName
                : baseName + amountOfCopies;

        LogoElement e = new LogoElement(
                cloneName,
                getPos(),
                getSize(),
                getRotateRatio()
        );
        return e;
    }

    private static GeneralPath buildLAPath() {
        // pravi LA logo u koordinatama 0..1:
        GeneralPath p = new GeneralPath();

        // --- L ---
        p.moveTo(0.15, 0.15);
        p.lineTo(0.15, 0.85);
        p.lineTo(0.38, 0.85);

        // --- A ---
        p.moveTo(0.62, 0.85);
        p.lineTo(0.74, 0.15);
        p.lineTo(0.86, 0.85);
        p.moveTo(0.68, 0.55);
        p.lineTo(0.80, 0.55);

        return p;
    }
}
