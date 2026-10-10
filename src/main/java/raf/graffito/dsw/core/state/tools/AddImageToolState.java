package raf.graffito.dsw.core.state.tools;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.command.slide.AddElementCommand;
import raf.graffito.dsw.core.model.factory.slideElement.SlideElementType;
import raf.graffito.dsw.core.model.images.ImageSource;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.state.ToolStateManager;
import raf.graffito.dsw.core.state.interfaces.ToolState;
import raf.graffito.dsw.core.state.tools.utils.PlacementUtil;   // ✅ NEW
import raf.graffito.dsw.gui.swing.views.editor.SlideView;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class AddImageToolState implements ToolState {

    private final ToolStateManager mgr = ApplicationFramework.getInstance().getToolStateManager();

    @Override
    public String getName() {
        return "ADD_IMAGE";
    }

    @Override
    public void mouseClicked(Slide slide, Point p, MouseEvent e) {
        if (slide == null || p == null) return;

        if (!ApplicationFramework.getInstance().getSlideSpacePolicy().assertCanAddElement(slide)) {
            return;
        }

        ImageSource src = mgr.getSelectedImageSource();
        if (src == null) return;

        BufferedImage img;
        try {
            img = src.loadFull();
        } catch (Exception ex) {
            return;
        }
        if (img == null) return;

        // fit image into a reasonable default box while keeping aspect ratio
        Dimension size = fitToMax(img.getWidth(), img.getHeight(), 420, 320);

        // ✅ WIRED: use shared smart placement util
        Point topLeft = PlacementUtil.smartPlace(slide, p, size, SlideView.SLIDE_W, SlideView.SLIDE_H, 10);
        if (topLeft == null) return; // no space

        SlideElement el = ApplicationFramework.getInstance()
                .getGraffRepoImplementation()
                .getSlideElementFactory()
                .create(SlideElementType.IMAGE, topLeft, size, 0, img);

        if (el == null) return;

        slide.getCommandManager().executeCommand(new AddElementCommand(slide, el));
    }

    private static Point centeredTopLeft(Point center, Dimension size) {
        return new Point(center.x - size.width / 2, center.y - size.height / 2);
    }

    private static Point smartPlace(Slide slide, Point click, Dimension size) {
        Point base = centeredTopLeft(click, size);
        base.x = Math.max(0, Math.min(base.x, SlideView.SLIDE_W - size.width));
        base.y = Math.max(0, Math.min(base.y, SlideView.SLIDE_H - size.height));

        List<SlideElement> elements = getSlideElements(slide);
        SlideElement hit = findHit(elements, click);

        if (hit == null) {
            Rectangle cand = new Rectangle(base.x, base.y, size.width, size.height);
            if (isFree(elements, cand, null)) return base;
            return searchAround(elements, click, size, null);
        }

        Rectangle hb = boundsOf(hit);
        int pad = 10;

        Point[] candidates = new Point[] {
                new Point(hb.x + hb.width + pad, hb.y + (hb.height - size.height) / 2),
                new Point(hb.x - size.width - pad, hb.y + (hb.height - size.height) / 2),
                new Point(hb.x + (hb.width - size.width) / 2, hb.y + hb.height + pad),
                new Point(hb.x + (hb.width - size.width) / 2, hb.y - size.height - pad)
        };

        for (Point tl : candidates) {
            Rectangle cand = new Rectangle(tl.x, tl.y, size.width, size.height);
            if (isInsideSlide(cand) && isFree(elements, cand, null)) return tl;
        }

        Point anchor = new Point(hb.x + hb.width / 2, hb.y + hb.height / 2);
        return searchAround(elements, anchor, size, null);
    }

    private static List<SlideElement> getSlideElements(Slide slide) {
        List<SlideElement> out = new ArrayList<>();
        for (GraffNode n : slide.getChildren()) {
            if (n instanceof SlideElement se) out.add(se);
        }
        return out;
    }

    private static SlideElement findHit(List<SlideElement> elements, Point p) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            SlideElement se = elements.get(i);
            if (boundsOf(se).contains(p)) return se;
        }
        return null;
    }

    private static Rectangle boundsOf(SlideElement e) {
        return new Rectangle(e.getPos().x, e.getPos().y, e.getSize().width, e.getSize().height);
    }

    private static boolean isInsideSlide(Rectangle r) {
        return r.x >= 0 && r.y >= 0
                && r.x + r.width <= SlideView.SLIDE_W
                && r.y + r.height <= SlideView.SLIDE_H;
    }

    private static boolean isFree(List<SlideElement> elements, Rectangle cand, SlideElement ignore) {
        for (SlideElement e : elements) {
            if (e == ignore) continue;
            if (boundsOf(e).intersects(cand)) return false;
        }
        return true;
    }

    private static Point searchAround(List<SlideElement> elements, Point anchorCenter, Dimension size, SlideElement ignore) {
        int step = 12;
        int maxR = Math.max(SlideView.SLIDE_W, SlideView.SLIDE_H);

        for (int r = step; r <= maxR; r += step) {
            for (int dx = -r; dx <= r; dx += step) {
                Point tlTop = centeredTopLeft(new Point(anchorCenter.x + dx, anchorCenter.y - r), size);
                Rectangle rt = new Rectangle(tlTop.x, tlTop.y, size.width, size.height);
                if (isInsideSlide(rt) && isFree(elements, rt, ignore)) return tlTop;

                Point tlBottom = centeredTopLeft(new Point(anchorCenter.x + dx, anchorCenter.y + r), size);
                Rectangle rb = new Rectangle(tlBottom.x, tlBottom.y, size.width, size.height);
                if (isInsideSlide(rb) && isFree(elements, rb, ignore)) return tlBottom;
            }
            for (int dy = -r; dy <= r; dy += step) {
                Point tlLeft = centeredTopLeft(new Point(anchorCenter.x - r, anchorCenter.y + dy), size);
                Rectangle rl = new Rectangle(tlLeft.x, tlLeft.y, size.width, size.height);
                if (isInsideSlide(rl) && isFree(elements, rl, ignore)) return tlLeft;

                Point tlRight = centeredTopLeft(new Point(anchorCenter.x + r, anchorCenter.y + dy), size);
                Rectangle rr = new Rectangle(tlRight.x, tlRight.y, size.width, size.height);
                if (isInsideSlide(rr) && isFree(elements, rr, ignore)) return tlRight;
            }
        }
        return null;
    }

    private static Dimension fitToMax(int w, int h, int maxW, int maxH) {
        if (w <= 0 || h <= 0) return new Dimension(Math.min(maxW, 200), Math.min(maxH, 150));

        double sx = (double) maxW / (double) w;
        double sy = (double) maxH / (double) h;
        double s = Math.min(1.0, Math.min(sx, sy)); // never upscale by default

        int nw = Math.max(1, (int) Math.round(w * s));
        int nh = Math.max(1, (int) Math.round(h * s));
        return new Dimension(nw, nh);
    }
}
