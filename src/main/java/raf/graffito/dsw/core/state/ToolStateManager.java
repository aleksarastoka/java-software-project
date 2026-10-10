package raf.graffito.dsw.core.state;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.core.model.images.ImageSource;
import raf.graffito.dsw.core.model.structures.Prototype;
import raf.graffito.dsw.core.state.interfaces.ToolState;
import raf.graffito.dsw.core.state.interfaces.WheelToolState;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.model.structures.TextBoxElement;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

@Getter @Setter
public class ToolStateManager {

    /// Glavna klasa za kontrolisanje stanjima, kada se na ui elementi selektuje stanje ovde se to namesti.

    private final java.util.Map<ToolType, ToolState> tools = new java.util.EnumMap<>(ToolType.class);
    private final List<Prototype> clipboard = new ArrayList<>();
    private ToolState current;
    private ToolType activeToolType;
    private Slide activeSlide;

    // ✅ used by ADD_IMAGE tool (proxy handle)
    private ImageSource selectedImageSource;

    // ✅ used by ADD_TEXT to trigger inline editing in SlideView
    private TextBoxElement pendingTextEdit;

    // ✅ selection state
    private final Set<SlideElement> selectedElements = new LinkedHashSet<>();

    // marquee selection rectangle (slide coordinates)
    private Rectangle marqueeRect;

    public void register(ToolType type, ToolState tool) {
        tools.put(type, tool);
        if (current == null) setActiveTool(type);
    }

    public void setActiveTool(ToolType type) {
        ToolState next = tools.get(type);
        if (next == null) return;

        this.activeToolType = type;
        this.current = next;
    }

    // ===== Selected image storage (NEW) =====
    public ImageSource getSelectedImageSource() { return selectedImageSource; }
    public void setSelectedImageSource(ImageSource src) { this.selectedImageSource = src; }

    // ===== Backward-compat convenience (OPTIONAL) =====
    // If some old code still calls getSelectedImage(), it will attempt full load.
    public BufferedImage getSelectedImage() {
        ImageSource src = selectedImageSource;
        if (src == null) return null;
        try {
            return src.loadFull();
        } catch (Exception ignored) {
            return src.getFallbackImage();
        }
    }
    public void setSelectedImage(BufferedImage ignored) {
        // Intentionally no-op: selection is now ImageSource-based.
        // Keeping this method prevents compile breaks if old code still calls it.
    }

    // ===== Inline edit request/consume =====
    public void requestTextEdit(TextBoxElement el) { this.pendingTextEdit = el; }
    public TextBoxElement consumeTextEdit() {
        TextBoxElement tmp = pendingTextEdit;
        pendingTextEdit = null;
        return tmp;
    }

    // ===== Selection helpers =====
    public Set<SlideElement> getSelectedElementsReadOnly() {
        return Collections.unmodifiableSet(selectedElements);
    }

    public void clearSelection() { selectedElements.clear(); }

    public void setSelection(SlideElement el) {
        selectedElements.clear();
        if (el != null) selectedElements.add(el);
    }

    public void addToSelection(SlideElement el) {
        if (el != null) selectedElements.add(el);
    }

    public void toggleSelection(SlideElement el) {
        if (el == null) return;
        if (selectedElements.contains(el)) selectedElements.remove(el);
        else selectedElements.add(el);
    }
    public void removeFromSelection(SlideElement el) {
        if (el == null) return;
        selectedElements.remove(el);
    }

    public void clearMarqueeRect() { marqueeRect = null; }

    // ===== Event forwarding =====
    public void mousePressed(Slide slide, Point slidePoint, MouseEvent e) {
        if (current != null) current.mousePressed(slide, slidePoint, e);
    }

    public void mouseDragged(Slide slide, Point slidePoint, MouseEvent e) {
        if (current != null) current.mouseDragged(slide, slidePoint, e);
    }

    public void mouseReleased(Slide slide, Point slidePoint, MouseEvent e) {
        if (current != null) current.mouseReleased(slide, slidePoint, e);
    }

    public void mouseClicked(Slide slide, Point slidePoint, MouseEvent e) {
        if (current != null) current.mouseClicked(slide, slidePoint, e);
    }

    public void mouseWheelMoved(Slide slide, Point p, MouseWheelEvent e) {
        if (current instanceof WheelToolState wheelTool) {
            wheelTool.mouseWheelMoved(slide, p, e);
        }
    }

    public void resetPerSlideState() {
        clearSelection();
        clearMarqueeRect();
        pendingTextEdit = null;
    }

    public void setActiveSlide(Slide slide) {
        if (this.activeSlide == slide) return;
        this.activeSlide = slide;
        resetPerSlideState();
    }

    public void pruneSelection(Slide slide) {
        if (slide == null) {
            clearSelection();
            return;
        }

        var kids = slide.getChildren();
        if (kids == null || kids.isEmpty()) {
            clearSelection();
            return;
        }

        selectedElements.removeIf(el -> !kids.contains(el));
    }
}
