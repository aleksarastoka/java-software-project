package raf.graffito.dsw.core.state.tools.utils;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PlacementUtil {

    private PlacementUtil() {}

    // --------- Public API (smart placement) ---------

    /**
     * Smart place:
     * - if click hits an element => try right/left/down/up next to it
     * - else => centered at click
     * - if blocked => expand search around anchor (hit element center or click)
     *
     * Returns TOP-LEFT of placement, or null if nothing fits.
     */
    public static Point smartPlace(Slide slide,
                                   Point click,
                                   Dimension size,
                                   int slideW,
                                   int slideH,
                                   int padding) {

        Point base = centeredTopLeft(click, size);
        base.x = clamp(base.x, 0, slideW - size.width);
        base.y = clamp(base.y, 0, slideH - size.height);

        List<SlideElement> elements = getSlideElements(slide);
        SlideElement hit = findTopmostAt(elements, click);

        // no hit => try centered base, otherwise search around click
        if (hit == null) {
            Rectangle cand = new Rectangle(base.x, base.y, size.width, size.height);
            if (isInside(cand, slideW, slideH) && isFree(elements, cand, null)) return base;
            return searchAround(elements, click, size, slideW, slideH, null);
        }

        Rectangle hb = boundsOf(hit);

        Point[] candidates = new Point[] {
                new Point(hb.x + hb.width + padding, hb.y + (hb.height - size.height) / 2),      // right
                new Point(hb.x - size.width - padding, hb.y + (hb.height - size.height) / 2),     // left
                new Point(hb.x + (hb.width - size.width) / 2, hb.y + hb.height + padding),        // down
                new Point(hb.x + (hb.width - size.width) / 2, hb.y - size.height - padding)       // up
        };

        for (Point tl : candidates) {
            Rectangle cand = new Rectangle(tl.x, tl.y, size.width, size.height);
            if (isInside(cand, slideW, slideH) && isFree(elements, cand, null)) return tl;
        }

        Point anchor = new Point(hb.x + hb.width / 2, hb.y + hb.height / 2);
        return searchAround(elements, anchor, size, slideW, slideH, null);
    }

    /**
     * Clone placement helper:
     * tries to place next to the ORIGINAL element, otherwise expands.
     * Uses an "occupied" list so multiple clones in one command don't collide.
     */
    public static Point smartPlaceNextTo(Rectangle anchorBounds,
                                         Dimension size,
                                         int slideW,
                                         int slideH,
                                         int padding,
                                         List<Rectangle> occupied) {

        Point[] candidates = new Point[] {
                new Point(anchorBounds.x + anchorBounds.width + padding,
                        anchorBounds.y + (anchorBounds.height - size.height) / 2), // right
                new Point(anchorBounds.x - size.width - padding,
                        anchorBounds.y + (anchorBounds.height - size.height) / 2), // left
                new Point(anchorBounds.x + (anchorBounds.width - size.width) / 2,
                        anchorBounds.y + anchorBounds.height + padding), // down
                new Point(anchorBounds.x + (anchorBounds.width - size.width) / 2,
                        anchorBounds.y - size.height - padding) // up
        };

        for (Point tl : candidates) {
            Rectangle cand = new Rectangle(tl.x, tl.y, size.width, size.height);
            if (isInside(cand, slideW, slideH) && isFree(occupied, cand)) return tl;
        }

        Point anchorCenter = new Point(anchorBounds.x + anchorBounds.width / 2,
                anchorBounds.y + anchorBounds.height / 2);

        return searchAround(occupied, anchorCenter, size, slideW, slideH);
    }

    // --------- Shared geometry helpers ---------

    public static Rectangle boundsOf(SlideElement e) {
        return new Rectangle(e.getPos().x, e.getPos().y, e.getSize().width, e.getSize().height);
    }

    public static List<SlideElement> getSlideElements(Slide slide) {
        List<SlideElement> out = new ArrayList<>();
        if (slide == null || slide.getChildren() == null) return out;

        for (GraffNode n : slide.getChildren()) {
            if (n instanceof SlideElement se) out.add(se);
        }
        return out;
    }

    public static SlideElement findTopmostAt(Slide slide, Point p) {
        return findTopmostAt(getSlideElements(slide), p);
    }

    public static SlideElement findTopmostAt(List<SlideElement> elements, Point p) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            SlideElement se = elements.get(i);
            if (boundsOf(se).contains(p)) return se;
        }
        return null;
    }

    public static List<Rectangle> collectStaticRects(Slide slide, Set<SlideElement> ignore) {
        List<Rectangle> rects = new ArrayList<>();
        if (slide == null || slide.getChildren() == null) return rects;

        for (GraffNode n : slide.getChildren()) {
            if (n instanceof SlideElement se) {
                if (ignore != null && ignore.contains(se)) continue;
                rects.add(boundsOf(se));
            }
        }
        return rects;
    }

    // --------- Internals ---------

    private static Point centeredTopLeft(Point center, Dimension size) {
        return new Point(center.x - size.width / 2, center.y - size.height / 2);
    }

    private static boolean isInside(Rectangle r, int w, int h) {
        return r.x >= 0 && r.y >= 0 && r.x + r.width <= w && r.y + r.height <= h;
    }

    private static boolean isFree(List<SlideElement> elements, Rectangle cand, SlideElement ignore) {
        for (SlideElement e : elements) {
            if (e == ignore) continue;
            if (boundsOf(e).intersects(cand)) return false;
        }
        return true;
    }

    private static boolean isFree(List<Rectangle> occupied, Rectangle cand) {
        for (Rectangle r : occupied) {
            if (r.intersects(cand)) return false;
        }
        return true;
    }

    private static Point searchAround(List<SlideElement> elements,
                                      Point anchorCenter,
                                      Dimension size,
                                      int slideW,
                                      int slideH,
                                      SlideElement ignore) {

        int step = 12;
        int maxR = Math.max(slideW, slideH);

        for (int r = step; r <= maxR; r += step) {
            for (int dx = -r; dx <= r; dx += step) {
                Point tlTop = centeredTopLeft(new Point(anchorCenter.x + dx, anchorCenter.y - r), size);
                Rectangle rt = new Rectangle(tlTop.x, tlTop.y, size.width, size.height);
                if (isInside(rt, slideW, slideH) && isFree(elements, rt, ignore)) return tlTop;

                Point tlBottom = centeredTopLeft(new Point(anchorCenter.x + dx, anchorCenter.y + r), size);
                Rectangle rb = new Rectangle(tlBottom.x, tlBottom.y, size.width, size.height);
                if (isInside(rb, slideW, slideH) && isFree(elements, rb, ignore)) return tlBottom;
            }

            for (int dy = -r; dy <= r; dy += step) {
                Point tlLeft = centeredTopLeft(new Point(anchorCenter.x - r, anchorCenter.y + dy), size);
                Rectangle rl = new Rectangle(tlLeft.x, tlLeft.y, size.width, size.height);
                if (isInside(rl, slideW, slideH) && isFree(elements, rl, ignore)) return tlLeft;

                Point tlRight = centeredTopLeft(new Point(anchorCenter.x + r, anchorCenter.y + dy), size);
                Rectangle rr = new Rectangle(tlRight.x, tlRight.y, size.width, size.height);
                if (isInside(rr, slideW, slideH) && isFree(elements, rr, ignore)) return tlRight;
            }
        }
        return null;
    }

    private static Point searchAround(List<Rectangle> occupied,
                                      Point anchorCenter,
                                      Dimension size,
                                      int slideW,
                                      int slideH) {

        int step = 12;
        int maxR = Math.max(slideW, slideH);

        for (int r = step; r <= maxR; r += step) {
            for (int dx = -r; dx <= r; dx += step) {
                Point tlTop = centeredTopLeft(new Point(anchorCenter.x + dx, anchorCenter.y - r), size);
                Rectangle rt = new Rectangle(tlTop.x, tlTop.y, size.width, size.height);
                if (isInside(rt, slideW, slideH) && isFree(occupied, rt)) return tlTop;

                Point tlBottom = centeredTopLeft(new Point(anchorCenter.x + dx, anchorCenter.y + r), size);
                Rectangle rb = new Rectangle(tlBottom.x, tlBottom.y, size.width, size.height);
                if (isInside(rb, slideW, slideH) && isFree(occupied, rb)) return tlBottom;
            }

            for (int dy = -r; dy <= r; dy += step) {
                Point tlLeft = centeredTopLeft(new Point(anchorCenter.x - r, anchorCenter.y + dy), size);
                Rectangle rl = new Rectangle(tlLeft.x, tlLeft.y, size.width, size.height);
                if (isInside(rl, slideW, slideH) && isFree(occupied, rl)) return tlLeft;

                Point tlRight = centeredTopLeft(new Point(anchorCenter.x + r, anchorCenter.y + dy), size);
                Rectangle rr = new Rectangle(tlRight.x, tlRight.y, size.width, size.height);
                if (isInside(rr, slideW, slideH) && isFree(occupied, rr)) return tlRight;
            }
        }
        return null;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(v, hi));
    }
}
