package raf.graffito.dsw.core.state.tools;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.command.CompositeCommand;
import raf.graffito.dsw.core.command.slide.RemoveElementCommand;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.state.ToolStateManager;
import raf.graffito.dsw.core.state.interfaces.ToolState;
import raf.graffito.dsw.gui.swing.tree.GraffTree;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DeleteToolState implements ToolState {

    private final ToolStateManager toolStateManager =
            ApplicationFramework.getInstance().getToolStateManager();

    @Override
    public String getName() {
        return "DELETE";
    }

    public void deleteSelection(Slide slide) {
        if (slide == null) return;

        Set<SlideElement> selected = toolStateManager.getSelectedElementsReadOnly();
        if (selected.isEmpty()) return;

        // copy selection so we don't mutate while iterating
        Set<SlideElement> toDelete = new HashSet<>(selected);

        // ✅ Build ONE command so Undo restores everything in one step
        CompositeCommand batch = new CompositeCommand("Delete Elements");
        for (SlideElement el : toDelete) {
            batch.add(new RemoveElementCommand(slide, el));
        }

        // Execute once -> one undo step
        slide.getCommandManager().executeCommand(batch);

        // Keep current tree behavior (delete nodes immediately)
        // NOTE: Undo/Redo won't restore/remove tree nodes until we add tree refresh/observer logic.

        toolStateManager.clearSelection();
    }

    @Override
    public void mouseClicked(Slide slide, Point slidePoint, MouseEvent e) {
        if (slide == null || slidePoint == null) return;

        // 1) if selection exists -> delete selected
        if (!toolStateManager.getSelectedElementsReadOnly().isEmpty()) {
            deleteSelection(slide);
            return;
        }

        // 2) if no selection -> click-to-delete element under cursor
        SlideElement hit = findElementAt(slide, slidePoint);
        if (hit == null) return;

        // ✅ execute undoable delete
        slide.getCommandManager().executeCommand(new RemoveElementCommand(slide, hit));

        // keep current tree behavior

        toolStateManager.clearSelection();
    }

    private SlideElement findElementAt(Slide slide, Point p) {
        if (!(slide instanceof raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite comp))
            return null;

        var kids = comp.getChildren();
        if (kids == null || kids.isEmpty()) return null;

        // from last to first (top-most)
        for (int i = kids.size() - 1; i >= 0; i--) {
            var n = kids.get(i);
            if (!(n instanceof SlideElement el)) continue;

            Rectangle r = new Rectangle(
                    el.getPos().x, el.getPos().y,
                    el.getSize().width, el.getSize().height
            );

            if (r.contains(p)) return el;
        }
        return null;
    }
}
