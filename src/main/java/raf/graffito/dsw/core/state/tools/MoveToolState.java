package raf.graffito.dsw.core.state.tools;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.command.CompositeCommand;
import raf.graffito.dsw.core.command.slide.MoveElementCommand;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.state.interfaces.ToolState;
import raf.graffito.dsw.core.state.tools.utils.PlacementUtil; // ✅ NEW
import raf.graffito.dsw.gui.swing.views.editor.SlideView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class MoveToolState implements ToolState {

    private boolean dragging = false;
    private Point dragStart;

    // identity-based map is important here
    private final Map<SlideElement, Point> startPositions = new IdentityHashMap<>();

    @Override
    public String getName() {
        return "MOVE";
    }

    @Override
    public void mousePressed(Slide slide, Point p, MouseEvent e) {
        if (slide == null || p == null) return;
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        var mgr = ApplicationFramework.getInstance().getToolStateManager();
        Set<SlideElement> selected = mgr.getSelectedElements();

        if (selected.isEmpty()) {
            SlideElement hit = PlacementUtil.findTopmostAt(slide, p); // ✅ WIRED
            if (hit != null) mgr.setSelection(hit);
            selected = mgr.getSelectedElements();
            if (selected.isEmpty()) return;
        }

        SlideElement hit = PlacementUtil.findTopmostAt(slide, p); // ✅ WIRED
        if (hit == null || !selected.contains(hit)) return;

        dragging = true;
        dragStart = p;

        startPositions.clear();
        for (SlideElement el : selected) {
            startPositions.put(el, new Point(el.getPos()));
        }
    }

    @Override
    public void mouseDragged(Slide slide, Point p, MouseEvent e) {
        if (!dragging || slide == null || p == null) return;

        int dx = p.x - dragStart.x;
        int dy = p.y - dragStart.y;

        int[] clamped = clampDelta(dx, dy);
        int[] safe = clampDeltaNoOverlap(slide, clamped[0], clamped[1]);

        for (Map.Entry<SlideElement, Point> entry : startPositions.entrySet()) {
            SlideElement el = entry.getKey();
            Point start = entry.getValue();

            el.getPos().x = start.x + safe[0];
            el.getPos().y = start.y + safe[1];
        }
    }

    @Override
    public void mouseReleased(Slide slide, Point p, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        if (dragging && slide != null && !startPositions.isEmpty()) {
            CompositeCommand batch = new CompositeCommand("Move Elements");
            boolean changed = false;

            for (Map.Entry<SlideElement, Point> entry : startPositions.entrySet()) {
                SlideElement el = entry.getKey();
                Point before = entry.getValue();
                Point after = new Point(el.getPos());

                if (!after.equals(before)) {
                    changed = true;
                    batch.add(new MoveElementCommand(slide, el, before, after));
                }
            }

            if (changed && !batch.isEmpty()) {
                slide.getCommandManager().executeCommand(batch);
            }
        }

        dragging = false;
        dragStart = null;
        startPositions.clear();
    }

    // ============================================================
    // Collision helpers (wired to PlacementUtil)
    // ============================================================

    private int[] clampDeltaNoOverlap(Slide slide, int dx, int dy) {
        if (dx == 0 && dy == 0) return new int[]{0, 0};

        // ✅ WIRED: static rect collection
        List<Rectangle> staticRects =
                PlacementUtil.collectStaticRects(slide, startPositions.keySet());

        if (isMoveSafe(dx, dy, staticRects)) return new int[]{dx, dy};

        int lo = 0, hi = 1024;
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            int sdx = scaleDelta(dx, mid);
            int sdy = scaleDelta(dy, mid);

            if (isMoveSafe(sdx, sdy, staticRects)) lo = mid;
            else hi = mid - 1;
        }

        return new int[]{scaleDelta(dx, lo), scaleDelta(dy, lo)};
    }

    private boolean isMoveSafe(int dx, int dy, List<Rectangle> staticRects) {
        for (Map.Entry<SlideElement, Point> entry : startPositions.entrySet()) {
            SlideElement el = entry.getKey();
            Point start = entry.getValue();

            Rectangle moved = new Rectangle(
                    start.x + dx,
                    start.y + dy,
                    el.getSize().width,
                    el.getSize().height
            );

            for (Rectangle stat : staticRects) {
                if (moved.intersects(stat)) return false;
            }
        }
        return true;
    }

    // ============================================================
    // Existing helpers (UNCHANGED)
    // ============================================================

    private int[] clampDelta(int dx, int dy) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (Map.Entry<SlideElement, Point> entry : startPositions.entrySet()) {
            SlideElement el = entry.getKey();
            Point start = entry.getValue();

            minX = Math.min(minX, start.x);
            minY = Math.min(minY, start.y);
            maxX = Math.max(maxX, start.x + el.getSize().width);
            maxY = Math.max(maxY, start.y + el.getSize().height);
        }

        int cdx = Math.max(-minX, Math.min(dx, SlideView.SLIDE_W - maxX));
        int cdy = Math.max(-minY, Math.min(dy, SlideView.SLIDE_H - maxY));

        return new int[]{cdx, cdy};
    }

    private static int scaleDelta(int d, int t1024) {
        if (d == 0 || t1024 == 0) return 0;
        double t = t1024 / 1024.0;
        return d > 0 ? (int) Math.floor(d * t) : (int) Math.ceil(d * t);
    }
}
