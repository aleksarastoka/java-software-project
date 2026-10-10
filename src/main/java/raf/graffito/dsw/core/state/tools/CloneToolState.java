package raf.graffito.dsw.core.state.tools;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.command.slide.CloneElementsCommand;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.Prototype;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.state.interfaces.ToolState;
import raf.graffito.dsw.core.state.tools.utils.PlacementUtil;  // ✅ NEW
import raf.graffito.dsw.gui.swing.views.editor.SlideView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class CloneToolState implements ToolState {

    private static final int PADDING = 10;

    @Override
    public String getName() {
        return "CLONE";
    }

    public void cloneSelectedNow(Slide slide) {
        if (slide == null) return;

        if (!ApplicationFramework.getInstance().getSlideSpacePolicy().assertCanAddElement(slide)) {
            return;
        }

        var mgr = ApplicationFramework.getInstance().getToolStateManager();
        var sel = mgr.getSelectedElementsReadOnly();
        if (sel.isEmpty()) return;

        pasteClones(slide, sel);
    }

    @Override
    public void mousePressed(Slide slide, Point p, MouseEvent e) {
        if (slide == null || p == null) return;
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        if (!ApplicationFramework.getInstance().getSlideSpacePolicy().assertCanAddElement(slide)) {
            return;
        }

        SlideElement hit = PlacementUtil.findTopmostAt(slide, p); // ✅ WIRED
        if (hit == null) return;

        pasteClones(slide, List.of(hit));
    }

    // ============================================================
    // Core clone logic (wired to PlacementUtil, helpers preserved)
    // ============================================================

    private void pasteClones(Slide slide, Collection<SlideElement> originals) {
        var mgr = ApplicationFramework.getInstance().getToolStateManager();

        // ✅ build occupied list via PlacementUtil
        List<Rectangle> occupied = PlacementUtil.collectStaticRects(slide, null);

        List<SlideElement> created = new ArrayList<>();

        for (SlideElement orig : originals) {
            Prototype proto = orig.clone();
            if (!(proto instanceof SlideElement cloneEl)) continue;

            Dimension size = cloneEl.getSize();

            // ✅ WIRED: shared smart placement next to original
            Point tl = PlacementUtil.smartPlaceNextTo(
                    PlacementUtil.boundsOf(orig),
                    size,
                    SlideView.SLIDE_W,
                    SlideView.SLIDE_H,
                    PADDING,
                    occupied
            );

            if (tl == null) continue;

            cloneEl.setPos(tl);
            created.add(cloneEl);

            // reserve space so next clone doesn't collide
            occupied.add(new Rectangle(tl.x, tl.y, size.width, size.height));
        }

        if (created.isEmpty()) return;

        slide.getCommandManager().executeCommand(
                new CloneElementsCommand(slide, created)
        );

        mgr.clearSelection();
        for (SlideElement c : created) mgr.addToSelection(c);
    }

    /* ============================================================
       Everything below this point is UNCHANGED (kept intentionally)
       ============================================================ */

    private SlideElement findTopmostAt(Slide slide, Point p) {
        var kids = slide.getChildren();
        if (kids == null) return null;

        for (int i = kids.size() - 1; i >= 0; i--) {
            Object node = kids.get(i);
            if (node instanceof SlideElement el) {
                if (elementContains(el, p)) return el;
            }
        }
        return null;
    }

    private boolean elementContains(SlideElement el, Point p) {
        Rectangle r = new Rectangle(el.getPos(), el.getSize());
        return r.contains(p);
    }
}
