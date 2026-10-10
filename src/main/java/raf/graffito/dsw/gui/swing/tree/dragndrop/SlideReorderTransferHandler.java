package raf.graffito.dsw.gui.swing.tree.dragndrop;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Presentation;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;

import static raf.graffito.dsw.core.model.decorator.Decorators.unwrap;

/*
    Klasa za kontrolisanje drag and drop funkcionalnosti
 */
public class SlideReorderTransferHandler extends TransferHandler {
    private static final DataFlavor PATHS_FLAVOR =
            new DataFlavor(TreePath[].class, "application/x-java-jtree-path-array");

    private final JTree tree;

    private DefaultMutableTreeNode dragParent = null;
    private DefaultMutableTreeNode dragNode   = null;
    private int dragIndex = -1;

    public SlideReorderTransferHandler(JTree tree) {
        this.tree = tree;
    }

    @Override public int getSourceActions(JComponent c) { return MOVE; }

    @Override
    protected Transferable createTransferable(JComponent c) {
        TreePath sel = tree.getSelectionPath();
        if (sel == null) return null;

        Object selNode = sel.getLastPathComponent();
        if (!(selNode instanceof GraffTreeItem gti)) return null;

        // UNWRAP the selected node and require a Slide
        Object domain = gti.getGraffNode();
        if (!(domain instanceof GraffNode gn)) return null;
        GraffNode core = unwrap(gn);
        if (!(core instanceof Slide)) return null;

        dragNode = (DefaultMutableTreeNode) selNode;
        dragParent = (DefaultMutableTreeNode) dragNode.getParent();
        if (dragParent == null) return null;

        // parent must be Presentation or Project (UNWRAP!)
        if (!(dragParent instanceof GraffTreeItem pgti)) return null;
        Object pDomain = pgti.getGraffNode();
        if (!(pDomain instanceof GraffNode pgn)) return null;
        GraffNode pCore = unwrap(pgn);
        if (!(pCore instanceof Presentation || pCore instanceof Project)) return null;

        dragIndex = dragParent.getIndex(dragNode);

        return new Transferable() {
            private final DataFlavor[] flavors = { PATHS_FLAVOR };
            @Override public DataFlavor[] getTransferDataFlavors() { return flavors; }
            @Override public boolean isDataFlavorSupported(DataFlavor f) { return PATHS_FLAVOR.equals(f); }
            @Override public Object getTransferData(DataFlavor f) { return new TreePath[]{ sel }; }
        };
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop()) return false;
        if (!support.isDataFlavorSupported(PATHS_FLAVOR)) return false;

        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        if (dl == null) return false;

        // Only INSERT between siblings of the same parent
        if (dl.getChildIndex() < 0) return false; // not ON
        TreePath dropPath = dl.getPath();
        if (dropPath == null) return false;

        // dropPath should be the parent (UNWRAP!)
        if (!(dropPath.getLastPathComponent() instanceof GraffTreeItem gtiParent)) return false;
        Object parentDom = gtiParent.getGraffNode();
        if (!(parentDom instanceof GraffNode pgn)) return false;
        GraffNode parentCore = unwrap(pgn);
        if (!(parentCore instanceof Presentation || parentCore instanceof Project)) return false;

        // same parent as the dragged node (UI identity)
        if (dragParent == null) return false;
        if (dropPath.getLastPathComponent() != dragParent) return false;

        support.setDropAction(MOVE);
        support.setShowDropLocation(true);
        return true;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;

        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        int targetIndex = dl.getChildIndex(); // index in parent where we insert

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

        // Adjust target if removing an item that was before the drop point
        int adjustedTarget = targetIndex;
        if (dragIndex < adjustedTarget) adjustedTarget--;

        // Domain reorder (UNWRAP parent, then call moveSlide)
        GraffTreeItem parentGti = (GraffTreeItem) dragParent;
        Object parentNodeObj = parentGti.getGraffNode();

        if (!(parentNodeObj instanceof GraffNode pgn)) return false;
        GraffNode parentCore = unwrap(pgn);

        if (parentCore instanceof Presentation pres) {
            pres.moveSlide(dragIndex, adjustedTarget);
        } else if (parentCore instanceof Project proj) {
            proj.moveSlide(dragIndex, adjustedTarget);
        } else {
            return false; // unsupported parent
        }

        // Swing reorder
        model.removeNodeFromParent(dragNode);
        model.insertNodeInto(dragNode, dragParent, Math.max(0, Math.min(adjustedTarget, dragParent.getChildCount())));

        // Reselect moved node
        TreePath newPath = new TreePath(dragNode.getPath());
        tree.setSelectionPath(newPath);
        tree.scrollPathToVisible(newPath);

        MainFrame.getInstance()
                .getProjectView()
                .orderChanged(parentGti.getGraffNode());

        return true;
    }
}
