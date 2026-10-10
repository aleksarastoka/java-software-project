package raf.graffito.dsw.core.state.tools;

import raf.graffito.dsw.core.state.interfaces.ToolState;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.model.structures.TextBoxElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SelectToolState implements ToolState {

    /// State za selektovanje elemenata, za omogucenom selekcijom vise elemenata

    private Point dragStart;
    private boolean marqueeActive = false;

    @Override
    public String getName() {
        return "SELECT";
    }

    @Override
    public void mouseClicked(Slide slide, Point p, MouseEvent e) {
        if (slide == null || p == null) return;
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        // ✅ NEW: prevent phantom selection after undo/remove
        pruneSelection(slide);

        // ✅ Double click on a TextBoxElement -> start inline edit
        if (e.getClickCount() == 2) {
            SlideElement hit = hitTestTopmost(slide, p);
            if (hit instanceof TextBoxElement tb) {
                var mgr = ApplicationFramework.getInstance().getToolStateManager();
                mgr.setSelection(tb);          // keep it selected
                mgr.requestTextEdit(tb);       // SlideView will consume + open editor
            }
        }
    }

    @Override
    public void mousePressed(Slide slide, Point p, MouseEvent e) {
        if (slide == null || p == null) return;
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        // ✅ NEW: prevent phantom selection after undo/remove
        pruneSelection(slide);

        dragStart = p;

        SlideElement hit = hitTestTopmost(slide, p);
        boolean shift = e.isShiftDown();

        // Click on an element -> normal selection
        if (hit != null) {
            marqueeActive = false;
            ApplicationFramework.getInstance().getToolStateManager().clearMarqueeRect();

            if (shift) {
                ApplicationFramework.getInstance().getToolStateManager().toggleSelection(hit);
            } else {
                ApplicationFramework.getInstance().getToolStateManager().setSelection(hit);
            }
            return;
        }

        // Click on empty -> marquee select
        marqueeActive = true;

        if (!shift) {
            ApplicationFramework.getInstance().getToolStateManager().clearSelection();
        }

        ApplicationFramework.getInstance().getToolStateManager()
                .setMarqueeRect(new Rectangle(p.x, p.y, 0, 0));
    }

    @Override
    public void mouseDragged(Slide slide, Point p, MouseEvent e) {
        if (!marqueeActive || dragStart == null || p == null) return;

        Rectangle r = rectFromPoints(dragStart, p);
        ApplicationFramework.getInstance().getToolStateManager().setMarqueeRect(r);
    }

    @Override
    public void mouseReleased(Slide slide, Point p, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        // ✅ NEW: prevent phantom selection after undo/remove
        if (slide != null) pruneSelection(slide);

        if (marqueeActive && dragStart != null && p != null && slide != null) {
            Rectangle r = rectFromPoints(dragStart, p);

            boolean shift = e.isShiftDown();
            List<SlideElement> inside = elementsIntersecting(slide, r);

            if (!shift) {
                ApplicationFramework.getInstance().getToolStateManager().clearSelection();
            }
            for (SlideElement el : inside) {
                ApplicationFramework.getInstance().getToolStateManager().addToSelection(el);
            }
        }

        marqueeActive = false;
        dragStart = null;
        ApplicationFramework.getInstance().getToolStateManager().clearMarqueeRect();
    }

    // ===================== NEW: selection pruning =====================

    private static void pruneSelection(Slide slide) {
        var mgr = ApplicationFramework.getInstance().getToolStateManager();
        var selected = mgr.getSelectedElements();
        if (selected == null || selected.isEmpty()) return;

        // Copy current selection
        List<SlideElement> keep = new ArrayList<>();
        boolean changed = false;

        for (SlideElement el : selected) {
            if (containsElement(slide, el)) {
                keep.add(el);
            } else {
                changed = true; // element no longer exists on this slide
            }
        }

        if (!changed) return;

        mgr.clearSelection();
        for (SlideElement el : keep) mgr.addToSelection(el);
    }

    private static boolean containsElement(Slide slide, SlideElement target) {
        if (slide == null || target == null) return false;
        if (slide.getChildren() == null) return false;

        for (Object n : slide.getChildren()) {
            if (n == target) return true; // identity match
        }
        return false;
    }

    // ===================== helpers =====================

    private static Rectangle rectFromPoints(Point a, Point b) {
        int x1 = Math.min(a.x, b.x);
        int y1 = Math.min(a.y, b.y);
        int x2 = Math.max(a.x, b.x);
        int y2 = Math.max(a.y, b.y);
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    private static Rectangle boundsOf(SlideElement el) {
        Point pos = el.getPos();
        Dimension sz = el.getSize();
        return new Rectangle(pos.x, pos.y, sz.width, sz.height);
    }

    // Topmost hit test (reverse order)
    private static SlideElement hitTestTopmost(Slide slide, Point p) {
        List<SlideElement> els = getElements(slide);
        for (int i = els.size() - 1; i >= 0; i--) {
            SlideElement el = els.get(i);
            if (boundsOf(el).contains(p)) return el;
        }
        return null;
    }

    private static List<SlideElement> elementsIntersecting(Slide slide, Rectangle r) {
        List<SlideElement> result = new ArrayList<>();
        for (SlideElement el : getElements(slide)) {
            if (r.intersects(boundsOf(el))) result.add(el);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<SlideElement> getElements(Slide slide) {
        List<SlideElement> els = new ArrayList<>();
        if (slide.getChildren() == null) return els;

        for (var n : slide.getChildren()) {
            if (n instanceof SlideElement el) els.add(el);
        }
        return els;
    }
}
