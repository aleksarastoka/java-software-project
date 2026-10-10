package raf.graffito.dsw.core.strategy;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;

public class AreaSumOccupancyStrategy implements OccupancyStrategy {

        @Override
        public double calculateOccupancy(Slide slide) {
            if (slide == null || slide.getChildren() == null) return 0.0;

            double occupied = 0;
            for (GraffNode n : slide.getChildren()) {
                if (n instanceof SlideElement el) {
                    occupied += el.getSize().width * el.getSize().height;
                }
            }


            double total = SlideArea.SLIDE_W * SlideArea.SLIDE_H;
            return occupied / total;
        }

}
