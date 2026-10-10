package raf.graffito.dsw.gui.swing.views.editor;

import lombok.Getter;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.command.slide.EditTextCommand;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.model.structures.TextBoxElement;
import raf.graffito.dsw.gui.swing.painter.SlidePainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

public class SlideCanvas extends JPanel {

    private final Slide slide;
    private final SlidePainter painter;

    private final DoubleSupplier zoomSupplier;
    private final BooleanSupplier autoFitSupplier;
    private final Runnable requestFit;
    private final Consumer<Boolean> onEditingChanged;

    private JTextField editor;
    private TextBoxElement editingElement;
    private String editingOriginalText;

    @Getter
    private boolean inlineEditing = false;

    public SlideCanvas(
            Slide slide,
            SlidePainter painter,
            DoubleSupplier zoomSupplier,
            BooleanSupplier autoFitSupplier,
            Runnable requestFit,
            Consumer<Boolean> onEditingChanged
    ) {
        this.slide = slide;
        this.painter = painter;
        this.zoomSupplier = zoomSupplier;
        this.autoFitSupplier = autoFitSupplier;
        this.requestFit = requestFit;
        this.onEditingChanged = onEditingChanged;

        setOpaque(true);
        setBackground(new Color(60, 60, 60));
        setLayout(null);
        setFocusable(true);

        setPreferredSize(new Dimension(
                SlideView.SLIDE_W + 2 * SlideView.OUTER_PADDING,
                SlideView.SLIDE_H + 2 * SlideView.OUTER_PADDING
        ));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (autoFitSupplier.getAsBoolean()) requestFit.run();
            }
        });

        MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = panelToSlide(e.getPoint());
                if (p == null) return;

                ApplicationFramework.getInstance()
                        .getToolStateManager()
                        .mouseClicked(slide, p, e);

                TextBoxElement toEdit = ApplicationFramework.getInstance()
                        .getToolStateManager()
                        .consumeTextEdit();

                if (toEdit != null) beginInlineEdit(toEdit);

                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Point p = panelToSlide(e.getPoint());
                if (p == null) return;

                // klik van editor-a commit-uje edit, pa tek onda tool obrada
                if (editor != null) endInlineEdit(true);

                ApplicationFramework.getInstance()
                        .getToolStateManager()
                        .mousePressed(slide, p, e);

                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = panelToSlide(e.getPoint());
                if (p == null) return;

                ApplicationFramework.getInstance()
                        .getToolStateManager()
                        .mouseDragged(slide, p, e);

                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point p = panelToSlide(e.getPoint());
                if (p == null) return;

                ApplicationFramework.getInstance()
                        .getToolStateManager()
                        .mouseReleased(slide, p, e);

                repaint();
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);

        addMouseWheelListener(e -> {
            Point p = panelToSlide(e.getPoint());
            if (p == null) return;

            ApplicationFramework.getInstance()
                    .getToolStateManager()
                    .mouseWheelMoved(slide, p, e);

            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            double zoom = zoomSupplier.getAsDouble();
            painter.paint(g2, slide, zoom, getWidth(), getHeight());
            drawSelectionOverlay(g2, zoom);
            drawMarqueeOverlay(g2, zoom);
        } finally {
            g2.dispose();
        }
    }

    private void drawSelectionOverlay(Graphics2D g2, double zoom) {
        var selected = ApplicationFramework.getInstance()
                .getToolStateManager()
                .getSelectedElementsReadOnly();

        if (selected.isEmpty()) return;

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(60, 140, 255));

        for (var el : selected) {
            Rectangle rSlide = rotatedAabbInSlide(el);
            Rectangle rPanel = slideRectToPanelRect(rSlide.getLocation(), rSlide.getSize(), zoom);
            g2.drawRect(rPanel.x, rPanel.y, rPanel.width, rPanel.height);
        }

        if (selected.size() == 1) {
            var el = selected.iterator().next();
            Rectangle rSlide = rotatedAabbInSlide(el);
            Rectangle rPanel = slideRectToPanelRect(rSlide.getLocation(), rSlide.getSize(), zoom);
            drawResizeHandles(g2, rPanel);
        }
    }

    private void drawMarqueeOverlay(Graphics2D g2, double zoom) {
        Rectangle rSlide = ApplicationFramework.getInstance()
                .getToolStateManager()
                .getMarqueeRect();

        if (rSlide == null) return;

        Rectangle r = slideRectToPanelRect(rSlide.getLocation(), rSlide.getSize(), zoom);

        float[] dash = {6f, 6f};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));

        g2.setColor(new Color(60, 140, 255, 200));
        g2.drawRect(r.x, r.y, r.width, r.height);

        g2.setColor(new Color(60, 140, 255, 40));
        g2.fillRect(r.x, r.y, r.width, r.height);
    }

    private void drawResizeHandles(Graphics2D g2, Rectangle rPanel) {
        int s = 10;
        int half = s / 2;

        Rectangle[] hs = new Rectangle[]{
                new Rectangle(rPanel.x - half, rPanel.y - half, s, s),
                new Rectangle(rPanel.x + rPanel.width - half, rPanel.y - half, s, s),
                new Rectangle(rPanel.x - half, rPanel.y + rPanel.height - half, s, s),
                new Rectangle(rPanel.x + rPanel.width - half, rPanel.y + rPanel.height - half, s, s)
        };

        g2.setColor(Color.WHITE);
        for (Rectangle h : hs) g2.fillRect(h.x, h.y, h.width, h.height);

        g2.setColor(new Color(60, 140, 255));
        for (Rectangle h : hs) g2.drawRect(h.x, h.y, h.width, h.height);
    }

    private Rectangle rotatedAabbInSlide(SlideElement el) {
        int w = el.getSize().width;
        int h = el.getSize().height;

        int rot = normalize90(el.getRotateRatio());
        boolean swapped = (rot % 180) != 0;

        int aabbW = swapped ? h : w;
        int aabbH = swapped ? w : h;

        int cx = el.getPos().x + w / 2;
        int cy = el.getPos().y + h / 2;

        int x = cx - aabbW / 2;
        int y = cy - aabbH / 2;

        return new Rectangle(x, y, aabbW, aabbH);
    }

    private int normalize90(int deg) {
        int d = deg % 360;
        if (d < 0) d += 360;
        if (d < 45 || d >= 315) return 0;
        if (d < 135) return 90;
        if (d < 225) return 180;
        return 270;
    }

    private void beginInlineEdit(TextBoxElement el) {
        endInlineEdit(false);

        editingElement = el;
        editingOriginalText = (el.getText() == null) ? "" : el.getText();

        editor = new JTextField(editingOriginalText);

        double zoom = zoomSupplier.getAsDouble();
        Rectangle r = slideRectToPanelRect(el.getPos(), el.getSize(), zoom);
        editor.setBounds(r);

        editor.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        add(editor);
        editor.requestFocusInWindow();

        inlineEditing = true;
        if (onEditingChanged != null) onEditingChanged.accept(true);

        editor.addActionListener(ev -> endInlineEdit(true));
        editor.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        editor.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                endInlineEdit(false);
            }
        });

        revalidate();
        repaint();
    }

    private void endInlineEdit(boolean commit) {
        if (editor == null) return;

        if (commit && editingElement != null) {
            String newText = (editor.getText() == null) ? "" : editor.getText();
            String oldText = (editingOriginalText == null) ? "" : editingOriginalText;

            if (!newText.equals(oldText)) {
                slide.getCommandManager().executeCommand(
                        new EditTextCommand(editingElement, oldText, newText)
                );
            }
        }

        remove(editor);
        editor = null;
        editingElement = null;
        editingOriginalText = null;

        inlineEditing = false;
        if (onEditingChanged != null) onEditingChanged.accept(false);

        revalidate();
        repaint();
    }

    private Rectangle slideRectToPanelRect(Point slidePos, Dimension slideSize, double zoom) {
        int slidePxW = (int) Math.round(SlideView.SLIDE_W * zoom);
        int slidePxH = (int) Math.round(SlideView.SLIDE_H * zoom);

        int x0 = (getWidth() - slidePxW) / 2;
        int y0 = (getHeight() - slidePxH) / 2;

        return new Rectangle(
                x0 + (int) Math.round(slidePos.x * zoom),
                y0 + (int) Math.round(slidePos.y * zoom),
                (int) Math.round(slideSize.width * zoom),
                (int) Math.round(slideSize.height * zoom)
        );
    }

    private Point panelToSlide(Point p) {
        double zoom = zoomSupplier.getAsDouble();

        int slidePxW = (int) Math.round(SlideView.SLIDE_W * zoom);
        int slidePxH = (int) Math.round(SlideView.SLIDE_H * zoom);

        int x0 = (getWidth() - slidePxW) / 2;
        int y0 = (getHeight() - slidePxH) / 2;

        int x = p.x - x0;
        int y = p.y - y0;

        if (x < 0 || y < 0 || x > slidePxW || y > slidePxH) return null;

        return new Point(
                (int) Math.round(x / zoom),
                (int) Math.round(y / zoom)
        );
    }
}
