package raf.graffito.dsw.controller.mediator;

public interface Colleague {
    /**
     * Called after screen mode is applied and UI scale has been applied.
     * uiScale is typically 1.0 (NORMAL/FULLSCREEN) or 0.5 (SMALL).
     */
    default void onScreenModeChanged(ScreenMode mode, double uiScale){};
}
