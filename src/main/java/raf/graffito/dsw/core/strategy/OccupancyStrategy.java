package raf.graffito.dsw.core.strategy;

import raf.graffito.dsw.core.model.repository.implementation.Slide;

public interface OccupancyStrategy {
    double calculateOccupancy(Slide slide);
}
