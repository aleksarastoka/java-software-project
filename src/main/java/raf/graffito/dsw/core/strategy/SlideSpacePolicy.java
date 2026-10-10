package raf.graffito.dsw.core.strategy;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.implementation.Slide;

@Setter
@Getter
public class SlideSpacePolicy {

    /** Only used to BLOCK ADDING new elements. Resizing is always allowed. */
    public static final double MAX_OCCUPIED_RATIO = 0.80;

    private OccupancyStrategy strategy;

    public SlideSpacePolicy(OccupancyStrategy strategy) {
        this.strategy = strategy;
    }

    /** Current occupied ratio in [0..1]. */
    public double occupiedRatio(Slide slide) {
        if (strategy == null) return 0.0;
        double r = strategy.calculateOccupancy(slide);
        if (Double.isNaN(r) || Double.isInfinite(r)) return 0.0;
        return Math.max(0.0, Math.min(1.0, r));
    }

    /** True if we are allowed to ADD new elements (<= 80%). */
    public boolean hasEnoughFreeSpaceForAdd(Slide slide) {
        return occupiedRatio(slide) <= MAX_OCCUPIED_RATIO;
    }

    /**
     * Use this ONLY for ADD operations.
     * - If over 80% => show message and deny add
     * - Resizing is NOT checked here by design.
     */
    public boolean assertCanAddElement(Slide slide) {
        if (!hasEnoughFreeSpaceForAdd(slide)) {
            ApplicationFramework.getInstance().getMg().generate("SLIDE_SPACE_FULL");
            return false;
        }
        return true;
    }

    /**
     * Resizing is always allowed, even if it pushes occupancy above 80%.
     * (You can add a warning message elsewhere if you want, but do not block.)
     */
    public boolean assertCanResize(Slide slide) {
        return true;
    }
}
