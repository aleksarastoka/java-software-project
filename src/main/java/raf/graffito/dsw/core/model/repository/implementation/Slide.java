package raf.graffito.dsw.core.model.repository.implementation;

import lombok.Getter;
import raf.graffito.dsw.core.command.CommandManager;
import raf.graffito.dsw.core.message.IPublisher;
import raf.graffito.dsw.core.message.ISubscriber;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite;
import raf.graffito.dsw.core.model.structures.SlideElement;

import java.util.ArrayList;
import java.util.List;

public class Slide extends GraffNodeComposite implements IPublisher<Slide> {
    /// predstavlja jedan slajd i sluzi kao kontejner za SlideElement-e, obavestava UI o promenama
    private final List<ISubscriber<Slide>> subscribers = new ArrayList<>();

    // ✅ Command access (Undo/Redo per slide)
    @Getter
    private final CommandManager commandManager = new CommandManager();

    // ------------------------------------------------------------------
    // NEW: last-change info so subscribers (tree, views) can mirror changes
    // without commands touching UI.
    // ------------------------------------------------------------------
    private transient GraffNode lastChangedChild = null;
    private transient boolean lastChangeWasAdd = false;
    private transient int lastChangedIndex = -1;

    public GraffNode getLastChangedChild() { return lastChangedChild; }
    public boolean wasLastChangeAdd() { return lastChangeWasAdd; }
    public int getLastChangedIndex() { return lastChangedIndex; }

    private void setLastChange(GraffNode child, boolean wasAdd, int index) {
        this.lastChangedChild = child;
        this.lastChangeWasAdd = wasAdd;
        this.lastChangedIndex = index;
    }

    private void clearLastChange() {
        this.lastChangedChild = null;
        this.lastChangedIndex = -1;
        // keep lastChangeWasAdd value irrelevant once child is null
    }

    public Slide(String name, GraffNode parent) {
        super(name, parent);
        // history changes should still update UI (undo/redo button state, repaint, etc.)
        commandManager.setOnHistoryChanged(() -> notifySubscribers(this));
    }

    // ✅ Optional convenience wrappers
    public void addElement(SlideElement element) {
        addChild(element);
    }

    public void removeElement(SlideElement element) {
        removeChild(element);
    }

    // ✅ NEW: insert element at a given index (for undo of remove)
    public void addElementAt(SlideElement element, int index) {
        if (element == null) return;

        // allow only SlideElement
        if (!(element instanceof SlideElement)) return;

        int size = getChildren().size();
        int idx = Math.max(0, Math.min(index, size));

        setLastChange(element, true, idx);

        getChildren().add(idx, element);
        element.setParent(this);

        notifySubscribers(this);

        clearLastChange();
    }

    @Override
    public void addChild(GraffNode child) {
        // allow only SlideElement
        if (!(child instanceof SlideElement)) {
            return;
        }

        int idx = getChildren().size();
        setLastChange(child, true, idx);

        getChildren().add(child);
        child.setParent(this);

        notifySubscribers(this);

        clearLastChange();
    }

    @Override
    public void removeChild(GraffNode child) {
        if (!(child instanceof SlideElement)) {
            return;
        }

        int idx = getChildren().indexOf(child);
        setLastChange(child, false, idx);

        getChildren().remove(child);
        child.setParent(null);

        notifySubscribers(this);

        clearLastChange();
    }

    // --- OBSERVER ---

    @Override
    public void addSubscriber(ISubscriber<Slide> subscriber) {
        if (subscriber != null && !subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    @Override
    public void removeSubscriber(ISubscriber<Slide> sub) {
        subscribers.remove(sub);
    }

    @Override
    public void notifySubscribers(Slide notification) {
        for (ISubscriber<Slide> s : new ArrayList<>(subscribers)) {
            s.update(notification);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
