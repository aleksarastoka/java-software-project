package raf.graffito.dsw.gui.swing.views.sidebar;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.images.ImageSource;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ImagePalettePanel extends JPanel {

    private static final int THUMB_W = 150;
    private static final int THUMB_H = 90;
    private static final int CELL_H  = 120;

    private final DefaultListModel<ImageIcon> model = new DefaultListModel<>();
    private final List<ImageSource> sources = new ArrayList<>();
    private final JList<ImageIcon> list = new JList<>(model);

    private final JScrollPane sp;

    private boolean active = true;

    public ImagePalettePanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Loaded images"));

        configureListUI();

        sp = new JScrollPane(list,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        sp.getVerticalScrollBar().setUnitIncrement(16);

        // Kad se menja visina sidebar-a (fullscreen/resize), osveži preferred size liste
        sp.getViewport().addChangeListener(e -> updateListPreferredSize());

        add(sp, BorderLayout.CENTER);

        // sugeriši širinu, visinu neka layout rešava
        setPreferredSize(new Dimension(180, 0));
        setMinimumSize(new Dimension(180, 0));

        updateListPreferredSize();
    }

    public void setActive(boolean active) {
        this.active = active;
        list.setEnabled(active);
        list.repaint();

        if (active) {
            int idx = list.getSelectedIndex();
            if (idx >= 0 && idx < sources.size()) {
                ApplicationFramework.getInstance()
                        .getToolStateManager()
                        .setSelectedImageSource(sources.get(idx));
            }
        }
    }

    public void addImageSource(ImageSource src) {
        if (src == null) return;

        sources.add(src);
        model.addElement(src.getThumbnailIcon());

        if (sources.size() == 1) {
            list.setSelectedIndex(0);
            if (active) {
                ApplicationFramework.getInstance()
                        .getToolStateManager()
                        .setSelectedImageSource(sources.get(0));
            }
        }

        updateListPreferredSize();
        revalidate();
        repaint();
    }

    public void addImageSources(List<? extends ImageSource> list) {
        if (list == null) return;
        for (ImageSource src : list) addImageSource(src);
    }

    public List<ImageSource> getImageSourcesReadOnly() {
        return List.copyOf(sources);
    }

    private void configureListUI() {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(0); // ✅ bitno: ne -1, nego 0 (let it compute)
        list.setFixedCellHeight(CELL_H);
        list.setFixedCellWidth(THUMB_W + 24);

        list.setCellRenderer((lst, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value);
            lbl.setOpaque(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setVerticalAlignment(SwingConstants.CENTER);
            lbl.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            if (!lst.isEnabled()) {
                lbl.setBackground(new Color(235, 235, 235));
            } else {
                lbl.setBackground(isSelected ? new Color(210, 210, 210) : Color.WHITE);
            }
            return lbl;
        });

        list.addListSelectionListener(e -> {
            if (!active) return;
            if (e.getValueIsAdjusting()) return;

            int idx = list.getSelectedIndex();
            if (idx >= 0 && idx < sources.size()) {
                ApplicationFramework.getInstance()
                        .getToolStateManager()
                        .setSelectedImageSource(sources.get(idx));
            }
        });
    }

    /**
     * ✅ KLJUČNO:
     * ScrollPane se vodi preferred size view-a.
     * Zato eksplicitno podešavamo visinu na broj elemenata * CELL_H.
     */
    private void updateListPreferredSize() {
        int n = model.getSize();

        // širina: uzmi širinu viewport-a ako postoji, inače fallback
        int w = sp.getViewport().getWidth();
        if (w <= 0) w = THUMB_W + 24;

        int h = Math.max(CELL_H, n * CELL_H);

        list.setPreferredSize(new Dimension(w, h));
        list.revalidate();
    }

    public static int thumbW() { return THUMB_W; }
    public static int thumbH() { return THUMB_H; }
}
