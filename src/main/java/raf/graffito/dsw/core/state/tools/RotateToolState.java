package raf.graffito.dsw.core.state.tools;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.command.CompositeCommand;
import raf.graffito.dsw.core.command.slide.RotateElementCommand;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.state.interfaces.WheelToolState;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.Set;

public class RotateToolState implements WheelToolState {

    private static final int STEP = 90;

    @Override
    public String getName() {
        return "ROTATE";
    }

    @Override
    public void mouseWheelMoved(Slide slide, Point p, MouseWheelEvent e) {
        if (slide == null) return;

        var mgr = ApplicationFramework.getInstance().getToolStateManager();
        Set<SlideElement> selected = mgr.getSelectedElements();

        int notches = e.getWheelRotation(); // + down, - up
        int delta = notches * STEP;
        if (delta == 0) return;

        // If nothing selected -> rotate hit element only
        if (selected == null || selected.isEmpty()) {
            SlideElement hit = hitTestTopmost(slide, p);
            if (hit == null) return;

            int before = hit.getRotateRatio();
            int after = normalize(before + delta);

            slide.getCommandManager().executeCommand(
                    new RotateElementCommand(slide, hit, before, after)
            );
            return;
        }

        // Rotate all selected as ONE undo step
        CompositeCommand batch = new CompositeCommand("Rotate Elements");

        boolean changed = false;
        for (SlideElement el : selected) {
            if (el == null) continue;

            int before = el.getRotateRatio();
            int after = normalize(before + delta);

            if (before != after) {
                changed = true;
                batch.add(new RotateElementCommand(slide, el, before, after));
            }
        }

        if (changed && !batch.isEmpty()) {
            slide.getCommandManager().executeCommand(batch);
        }
    }

    private static int normalize(int r) {
        r %= 360;
        if (r < 0) r += 360;
        return r;
    }

    private static SlideElement hitTestTopmost(Slide slide, Point p) {
        if (slide.getChildren() == null || p == null) return null;

        for (int i = slide.getChildren().size() - 1; i >= 0; i--) {
            var n = slide.getChildren().get(i);
            if (n instanceof SlideElement el) {
                Rectangle r = new Rectangle(el.getPos().x, el.getPos().y, el.getSize().width, el.getSize().height);
                if (r.contains(p)) return el;
            }
        }
        return null;
    }
}
