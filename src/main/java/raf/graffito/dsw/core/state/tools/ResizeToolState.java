package raf.graffito.dsw.core.state.tools;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.command.slide.ResizeElementCommand;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.state.interfaces.ToolState;
import raf.graffito.dsw.gui.swing.views.editor.SlideView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ResizeToolState implements ToolState {

    private enum Handle { NW, NE, SW, SE }

    private static final int HANDLE_HALF = 6; // handle ~12x12
    private static final int MIN_W = 30;
    private static final int MIN_H = 20;

    // NOTE: resizing is allowed to go above 80% now,
    // so this constant is no longer used for blocking.
    private static final double MAX_MATRIX_OCCUPIED = 0.80;

    private boolean resizing = false;
    private Handle activeHandle;

    private SlideElement target;
    private Point startMouse;
    private Point startPos;
    private Dimension startSize;

    private List<Rectangle> staticRects; // all other elements (exclude target)

    @Override
    public String getName() {
        return "RESIZE";
    }

    @Override
    public void mousePressed(Slide slide, Point p, MouseEvent e) {
        if (slide == null || p == null) return;
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        var mgr = ApplicationFramework.getInstance().getToolStateManager();
        Set<SlideElement> selected = mgr.getSelectedElements(); // Lombok getter

        if (selected == null || selected.size() != 1) return;

        SlideElement el = selected.iterator().next();
        Handle h = hitHandle(el, p);
        if (h == null) return;

        resizing = true;
        activeHandle = h;
        target = el;

        startMouse = new Point(p);
        startPos = new Point(el.getPos());
        startSize = new Dimension(el.getSize());

        // Build static rects: everything except the target
        staticRects = new ArrayList<>();
        if (slide.getChildren() != null) {
            for (Object node : slide.getChildren()) {
                if (node instanceof SlideElement other) {
                    if (other == target) continue;
                    staticRects.add(boundsOf(other));
                }
            }
        }
    }

    @Override
    public void mouseDragged(Slide slide, Point p, MouseEvent e) {
        if (!resizing || target == null || p == null) return;

        int dx = p.x - startMouse.x;
        int dy = p.y - startMouse.y;

        // Desired rect under this mouse delta (min size + slide bounds clamped)
        Rectangle desired = computeRectForDelta(dx, dy);

        // If collision-free, apply directly
        if (isFree(desired)) {
            applyRect(desired);
            return;
        }

        // Otherwise find max safe resize along the drag vector
        Rectangle safe = findLargestSafeRect(dx, dy);
        if (safe != null) applyRect(safe);
        // If safe is null, stay at start state (implicitly)
    }

    @Override
    public void mouseReleased(Slide slide, Point p, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        if (resizing && slide != null && target != null && startPos != null && startSize != null) {

            Point afterPos = new Point(target.getPos());
            Dimension afterSize = new Dimension(target.getSize());

            boolean changed =
                    !afterPos.equals(startPos) ||
                            afterSize.width != startSize.width ||
                            afterSize.height != startSize.height;

            // ✅ CHANGED: resizing is ALWAYS allowed (even above 80% occupancy)
            // We only block ADD operations, not RESIZE.
            if (changed) {
                slide.getCommandManager().executeCommand(
                        new ResizeElementCommand(slide, target, startPos, startSize, afterPos, afterSize)
                );
            }
        }

        resizing = false;
        activeHandle = null;
        target = null;
        startMouse = null;
        startPos = null;
        startSize = null;
        staticRects = null;
    }

    // ---------- Collision-aware resize core ----------

    private Rectangle findLargestSafeRect(int dx, int dy) {
        // If even the start rect overlaps (shouldn’t), bail
        Rectangle startR = new Rectangle(startPos.x, startPos.y, startSize.width, startSize.height);
        if (!isFree(startR)) return startR;

        // Binary search t in [0..1024]
        int lo = 0;
        int hi = 1024;

        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;

            int sdx = scaleDelta(dx, mid);
            int sdy = scaleDelta(dy, mid);

            Rectangle cand = computeRectForDelta(sdx, sdy);

            if (isFree(cand)) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }

        return computeRectForDelta(scaleDelta(dx, lo), scaleDelta(dy, lo));
    }

    private Rectangle computeRectForDelta(int dx, int dy) {
        int x = startPos.x;
        int y = startPos.y;
        int w = startSize.width;
        int h = startSize.height;

        switch (activeHandle) {
            case SE -> { w = startSize.width + dx; h = startSize.height + dy; }
            case SW -> { x = startPos.x + dx; w = startSize.width - dx; h = startSize.height + dy; }
            case NE -> { y = startPos.y + dy; w = startSize.width + dx; h = startSize.height - dy; }
            case NW -> { x = startPos.x + dx; y = startPos.y + dy; w = startSize.width - dx; h = startSize.height - dy; }
        }

        // ---- Min size clamp (preserve anchored corner behavior) ----
        if (w < MIN_W) {
            int diff = MIN_W - w;
            w = MIN_W;
            if (activeHandle == Handle.NW || activeHandle == Handle.SW) x -= diff;
        }
        if (h < MIN_H) {
            int diff = MIN_H - h;
            h = MIN_H;
            if (activeHandle == Handle.NW || activeHandle == Handle.NE) y -= diff;
        }

        // ---- Slide bounds clamp ----
        if (x < 0) {
            int push = -x;
            x = 0;
            if (activeHandle == Handle.NW || activeHandle == Handle.SW) {
                w = Math.max(MIN_W, w - push);
            }
        }
        if (y < 0) {
            int push = -y;
            y = 0;
            if (activeHandle == Handle.NW || activeHandle == Handle.NE) {
                h = Math.max(MIN_H, h - push);
            }
        }

        if (x + w > SlideView.SLIDE_W) w = Math.max(MIN_W, SlideView.SLIDE_W - x);
        if (y + h > SlideView.SLIDE_H) h = Math.max(MIN_H, SlideView.SLIDE_H - y);

        return new Rectangle(x, y, w, h);
    }

    private void applyRect(Rectangle r) {
        target.getPos().x = r.x;
        target.getPos().y = r.y;
        target.getSize().width = r.width;
        target.getSize().height = r.height;
    }

    private boolean isFree(Rectangle cand) {
        if (staticRects == null) return true;
        for (Rectangle r : staticRects) {
            if (r.intersects(cand)) return false;
        }
        return true;
    }

    private static int scaleDelta(int d, int t1024) {
        if (d == 0 || t1024 == 0) return 0;
        double t = t1024 / 1024.0;
        return d > 0 ? (int) Math.floor(d * t) : (int) Math.ceil(d * t);
    }

    // ---------- Handle hit-testing in SLIDE COORDS ----------
    private static Handle hitHandle(SlideElement el, Point p) {
        Rectangle b = boundsOf(el);

        Rectangle nw = new Rectangle(b.x - HANDLE_HALF, b.y - HANDLE_HALF, HANDLE_HALF * 2, HANDLE_HALF * 2);
        Rectangle ne = new Rectangle(b.x + b.width - HANDLE_HALF, b.y - HANDLE_HALF, HANDLE_HALF * 2, HANDLE_HALF * 2);
        Rectangle sw = new Rectangle(b.x - HANDLE_HALF, b.y + b.height - HANDLE_HALF, HANDLE_HALF * 2, HANDLE_HALF * 2);
        Rectangle se = new Rectangle(b.x + b.width - HANDLE_HALF, b.y + b.height - HANDLE_HALF, HANDLE_HALF * 2, HANDLE_HALF * 2);

        if (nw.contains(p)) return Handle.NW;
        if (ne.contains(p)) return Handle.NE;
        if (sw.contains(p)) return Handle.SW;
        if (se.contains(p)) return Handle.SE;
        return null;
    }

    private static Rectangle boundsOf(SlideElement el) {
        Point pos = el.getPos();
        Dimension sz = el.getSize();
        return new Rectangle(pos.x, pos.y, sz.width, sz.height);
    }
}
