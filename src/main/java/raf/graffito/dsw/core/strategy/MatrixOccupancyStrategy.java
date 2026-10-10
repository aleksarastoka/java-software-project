package raf.graffito.dsw.core.strategy;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;

import java.awt.*;

public class MatrixOccupancyStrategy implements OccupancyStrategy {

    @Override
    public double calculateOccupancy(Slide slide) {
        if (slide == null || slide.getChildren() == null) return 0.0;

        int W = SlideArea.SLIDE_W;
        int H = SlideArea.SLIDE_H;

        boolean[][] occ = new boolean[H][W];

        for (GraffNode n : slide.getChildren()) {
            if (!(n instanceof SlideElement el)) continue;
            Point p = el.getPos();
            Dimension d = el.getSize();
            if (p == null || d == null) continue;

            int x0 = Math.max(0, p.x);
            int y0 = Math.max(0, p.y);
            int x1 = Math.min(W, p.x + d.width);
            int y1 = Math.min(H, p.y + d.height);

            for (int y = y0; y < y1; y++) {
                for (int x = x0; x < x1; x++) {
                    occ[y][x] = true;
                }
            }
        }

        long count = 0;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                if (occ[y][x]) count++;
            }
        }

        long total = (long) W * (long) H;
        return (double) count / (double) total;
    }
}
