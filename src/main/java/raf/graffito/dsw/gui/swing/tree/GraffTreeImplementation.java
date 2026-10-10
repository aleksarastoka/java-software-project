// raf/graffito/dsw/gui/swing/tree/GraffTreeImplementation.java
package raf.graffito.dsw.gui.swing.tree;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.message.ISubscriber;
import raf.graffito.dsw.core.model.factory.graffNode.ChildType;
import raf.graffito.dsw.core.model.repository.GraffRepository;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.repository.implementation.Workspace;
import raf.graffito.dsw.gui.swing.dialogs.ChooseChildTypeDialog;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.tree.view.GraffTreeView;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static raf.graffito.dsw.core.model.decorator.Decorators.unwrap;

@Getter
@Setter
public class GraffTreeImplementation implements GraffTree, ISubscriber<Slide> {

    private final GraffRepository repo;
    private GraffTreeView graffTreeView;
    private DefaultTreeModel treeModel;

    // NEW: observers
    private final List<TreeChangeListener> listeners = new ArrayList<>();
    public void addTreeChangeListener(TreeChangeListener l){ if (l!=null) listeners.add(l); }
    public void removeTreeChangeListener(TreeChangeListener l){ listeners.remove(l); }
    private void fireNodeAdded(GraffNode parent, GraffNode child, int index){
        for (var l : listeners) l.nodeAdded(parent, child, index);
    }
    private void fireNodeRemoved(GraffNode parent, GraffNode child, int formerIndex){
        for (var l : listeners) l.nodeRemoved(parent, child, formerIndex);
    }
    public void notifyNodeRenamed(GraffNode node, String oldName, String newName){
        for (var l : listeners) l.nodeRenamed(node, oldName, newName);
    }

    public GraffTreeImplementation(GraffRepository repo) {
        this.repo = Objects.requireNonNull(repo, "GraffRepository must not be null");
    }

    // Subscribe to all slides in the model tree so we can mirror SlideElement changes.
    private void subscribeToSlidesRecursively(GraffNode node) {
        if (node == null) return;
        GraffNode core = unwrap(node);

        if (core instanceof Slide s) {
            s.addSubscriber(this);
        }

        if (core instanceof GraffNodeComposite comp && comp.getChildren() != null) {
            for (GraffNode child : comp.getChildren()) {
                subscribeToSlidesRecursively(child);
            }
        }
    }

    @Override
    public GraffTreeView generateTree(Workspace workspace) {
        GraffTreeItem rootItem = new GraffTreeItem(workspace);
        treeModel = new DefaultTreeModel(rootItem);
        graffTreeView = new GraffTreeView(treeModel);

        // subscribe to existing slides
        subscribeToSlidesRecursively(workspace);

        return graffTreeView;
    }

    @Override
    public void addChild(GraffTreeItem parentItem) {
        Objects.requireNonNull(parentItem, "No node is selected.");

        GraffNode parentCore = unwrap(parentItem.getGraffNode());
        if (!(parentCore instanceof GraffNodeComposite)) {
            throw new IllegalArgumentException("Selected node cannot have children.");
        }
        GraffNodeComposite parentModel = (GraffNodeComposite) parentCore;

        GraffNode childCore;
        if (parentModel instanceof Project) {
            ChooseChildTypeDialog dlg =
                    new ChooseChildTypeDialog(SwingUtilities.getWindowAncestor(graffTreeView));
            ChooseChildTypeDialog.Result res = dlg.showDialog();
            if (res == ChooseChildTypeDialog.Result.CANCEL) return;

            ChildType type = (res == ChooseChildTypeDialog.Result.PRESENTATION)
                    ? ChildType.PRESENTATION : ChildType.SLIDE;

            childCore = repo.addChild(parentModel, type);
        } else {
            childCore = repo.addChild(parentModel);
        }

        // UI mirror
        GraffTreeItem childItem = new GraffTreeItem(childCore);
        createNode(parentItem, childItem);

        // subscribe if this is a Slide
        if (unwrap(childCore) instanceof Slide s) {
            s.addSubscriber(this);
        }

        int modelIndex = -1;
        if (parentModel.getChildren() != null) {
            modelIndex = parentModel.getChildren().indexOf(unwrap(childCore));
        }
        if (modelIndex < 0) {
            modelIndex = parentItem.getChildCount() - 1;
        }

        fireNodeAdded(parentCore, unwrap(childCore), modelIndex);
    }

    public GraffTreeItem getSelectedNode() {
        return (GraffTreeItem) graffTreeView.getLastSelectedPathComponent();
    }

    @Override
    public GraffTreeItem findGraffTreeItem(GraffTreeItem item, GraffNode graffNode) {
        if (item == null || graffNode == null) return null;

        if (item.getGraffNode() == graffNode) {
            return item;
        }

        for (int i = 0; i < item.getChildCount(); i++) {
            GraffTreeItem child = (GraffTreeItem) item.getChildAt(i);
            GraffTreeItem result = findGraffTreeItem(child, graffNode);
            if (result != null) return result;
        }

        return null;
    }

    @Override
    public GraffTreeItem getRoot() {
        return (GraffTreeItem) graffTreeView.getModel().getRoot();
    }

    public void createNode(GraffTreeItem parentItem, GraffTreeItem childItem) {
        treeModel.insertNodeInto(childItem, parentItem, parentItem.getChildCount());

        TreePath parentPath = new TreePath(parentItem.getPath());
        TreePath childPath  = new TreePath(childItem.getPath());
        graffTreeView.expandPath(parentPath);
        graffTreeView.setSelectionPath(childPath);
        graffTreeView.scrollPathToVisible(childPath);
    }

    @Override
    public void removeNode(GraffTreeItem node) {
        if (node == null) return;

        TreeNode parent = node.getParent();

        if (parent == null) {
            ApplicationFramework.getInstance().getMg()
                    .generate("NODE_CANNOT_BE_DELETED");
            return;
        }

        if (!(parent instanceof GraffTreeItem parentItem)) {
            ApplicationFramework.getInstance().getMg()
                    .generate("NODE_CANNOT_BE_DELETED");
            return;
        }

        GraffNode parentNode = parentItem.getGraffNode();
        GraffNode childNode  = node.getGraffNode();

        if (parentNode instanceof GraffNodeComposite compositeParent) {
            compositeParent.removeChild(childNode);
        } else {
            ApplicationFramework.getInstance().getMg()
                    .generate("NODE_CANNOT_BE_DELETED");
            return;
        }

        parentItem.remove(node);
        treeModel.reload(parentItem);
    }

    @Override
    public void deleteChild(GraffTreeItem item) {
        if (item == null) {
            ApplicationFramework.getInstance().getMg().generate("NO_NODE_SELECTED");
            return;
        }

        GraffNode childCore = unwrap(item.getGraffNode());
        if (childCore instanceof Workspace) {
            ApplicationFramework.getInstance().getMg().generate("NODE_CANNOT_BE_DELETED");
            return;
        }

        GraffTreeItem parentItem = (GraffTreeItem) item.getParent();
        if (parentItem == null) {
            ApplicationFramework.getInstance().getMg().generate("NODE_CANNOT_BE_DELETED");
            return;
        }

        GraffNode parentCore = unwrap(parentItem.getGraffNode());
        int formerIndex = parentItem.getIndex(item);

        try {
            // SlideElement deletion must go through command pattern
            if (childCore instanceof raf.graffito.dsw.core.model.structures.SlideElement se) {

                GraffNode p = se.getParent();
                if (!(p instanceof Slide slide)) {
                    throw new IllegalStateException("SlideElement has no Slide parent.");
                }

                slide.getCommandManager().executeCommand(
                        new raf.graffito.dsw.core.command.slide.RemoveElementCommand(slide, se)
                );

                // Tree update will happen via Slide subscriber -> update() -> mirrorModelNodeRemoved(...)
                return;
            }

            // Default: repo deletion for projects/presentations/slides/etc
            repo.deleteChild(childCore);

        } catch (Exception ex) {
            ex.printStackTrace();
            ApplicationFramework.getInstance().getMg().generate("DELETE_FAILED");
            return;
        }

        // Non-slide-elements: remove tree node here
        treeModel.removeNodeFromParent(item);

        fireNodeRemoved(parentCore, childCore, formerIndex);

        if (parentItem.getChildCount() > 0) {
            int selectIndex = Math.min(formerIndex, parentItem.getChildCount() - 1);
            GraffTreeItem toSelect = (GraffTreeItem) parentItem.getChildAt(selectIndex);
            TreePath selPath = new TreePath(toSelect.getPath());
            graffTreeView.setSelectionPath(selPath);
            graffTreeView.scrollPathToVisible(selPath);
        } else {
            TreePath parentPath = new TreePath(parentItem.getPath());
            graffTreeView.setSelectionPath(parentPath);
            graffTreeView.scrollPathToVisible(parentPath);
        }
    }

    // ----------------------------------------------------------------------
    // Subscriber: Slide notifies us; we mirror SlideElement adds/removes.
    // ----------------------------------------------------------------------
    @Override
    public void update(Slide slide) {
        if (slide == null) return;

        GraffNode changedChild = slide.getLastChangedChild();
        if (changedChild == null) return; // could be history-changed notification etc.

        if (slide.wasLastChangeAdd()) {
            mirrorModelNodeAdded(slide, changedChild);
        } else {
            mirrorModelNodeRemoved(slide, changedChild);
        }
    }

    // ----------------------------------------------------------------------
    // Bridge methods for model changes triggered outside the tree layer
    // ----------------------------------------------------------------------

    public void mirrorModelNodeAdded(GraffNode parent, GraffNode child) {
        if (parent == null || child == null || treeModel == null) return;

        SwingUtilities.invokeLater(() -> {
            GraffTreeItem root = getRoot();
            GraffTreeItem parentItem = findGraffTreeItem(root, parent);
            if (parentItem == null) return;

            for (int i = 0; i < parentItem.getChildCount(); i++) {
                GraffTreeItem existing = (GraffTreeItem) parentItem.getChildAt(i);
                if (existing.getGraffNode() == child) return;
            }

            GraffTreeItem childItem = new GraffTreeItem(child);

            int insertIndex = parentItem.getChildCount();
            GraffNode parentCore = unwrap(parent);
            GraffNode childCore = unwrap(child);

            if (parentCore instanceof GraffNodeComposite comp && comp.getChildren() != null) {
                int modelIndex = comp.getChildren().indexOf(childCore);
                if (modelIndex >= 0 && modelIndex <= parentItem.getChildCount()) {
                    insertIndex = modelIndex;
                }
            }

            treeModel.insertNodeInto(childItem, parentItem, insertIndex);

            // If we just added a Slide node, subscribe to it
            if (childCore instanceof Slide s) {
                s.addSubscriber(this);
            }

            TreePath parentPath = new TreePath(parentItem.getPath());
            TreePath childPath  = new TreePath(childItem.getPath());
            graffTreeView.expandPath(parentPath);
            graffTreeView.setSelectionPath(childPath);
            graffTreeView.scrollPathToVisible(childPath);

            fireNodeAdded(parentCore, childCore, insertIndex);
        });
    }

    public void mirrorModelNodeRemoved(GraffNode parent, GraffNode child) {
        if (parent == null || child == null || treeModel == null) return;

        SwingUtilities.invokeLater(() -> {
            GraffTreeItem root = getRoot();
            GraffTreeItem parentItem = findGraffTreeItem(root, parent);
            if (parentItem == null) return;

            GraffNode parentCore = unwrap(parent);
            GraffNode childCore = unwrap(child);

            GraffTreeItem childItem = null;
            int formerIndex = -1;

            for (int i = 0; i < parentItem.getChildCount(); i++) {
                GraffTreeItem c = (GraffTreeItem) parentItem.getChildAt(i);
                if (unwrap(c.getGraffNode()) == childCore) {
                    childItem = c;
                    formerIndex = i;
                    break;
                }
            }

            if (childItem == null) return;

            treeModel.removeNodeFromParent(childItem);

            // If we removed a Slide node, unsubscribe
            if (childCore instanceof Slide s) {
                s.removeSubscriber(this);
            }

            fireNodeRemoved(parentCore, childCore, formerIndex);
        });
    }
}
