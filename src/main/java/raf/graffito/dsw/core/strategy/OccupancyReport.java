package raf.graffito.dsw.core.strategy;

import raf.graffito.dsw.core.model.repository.implementation.Slide;

public final class OccupancyReport {

    private static final AreaSumOccupancyStrategy SUM = new AreaSumOccupancyStrategy();
    private static final MatrixOccupancyStrategy MAT = new MatrixOccupancyStrategy();

    private OccupancyReport() {}

    // nacin 1
    public static long sumPixels(Slide slide) {
        double ratio = SUM.calculateOccupancy(slide);
        long slideArea = slideAreaPx();
        return Math.round(ratio * slideArea);
    }

    // nacin 2
    public static int matrixPercent(Slide slide) {
        double ratio = MAT.calculateOccupancy(slide); // 0..1
        return toPercentClamped(ratio);
    }

    public static double matrixRatio(Slide slide) {
        return MAT.calculateOccupancy(slide);
    }

    private static long slideAreaPx() {
        return (long) raf.graffito.dsw.gui.swing.views.editor.SlideView.SLIDE_W
                * (long) raf.graffito.dsw.gui.swing.views.editor.SlideView.SLIDE_H;
    }

    private static int toPercentClamped(double ratio) {
        if (Double.isNaN(ratio) || Double.isInfinite(ratio)) return 0;
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        return (int) Math.round(ratio * 100.0);
    }
}
