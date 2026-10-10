package raf.graffito.dsw.controller.mediator;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ScreenSizeMediator {

    private static final String ORIG_FONT_KEY = "origFont";
    private static final String ORIG_PREF_KEY = "origPrefSize";
    private static final String ORIG_MIN_KEY  = "origMinSize";
    private static final String ORIG_MAX_KEY  = "origMaxSize";

    // ✅ NEW: store full/original text so ellipsis can be reversed
    private static final String FULL_TEXT_KEY = "fullText";

    private final JFrame frame;
    private final List<Colleague> explicitColleagues = new ArrayList<>();

    private final Dimension normalSize;
    private final Point normalLocation;
    private boolean applying = false;

    @Getter
    private ScreenMode mode = ScreenMode.NORMAL;

    @Setter
    private Container uiRoot;

    public ScreenSizeMediator(JFrame frame) {
        this.frame = frame;
        this.normalSize = frame.getSize();
        this.normalLocation = frame.getLocation();
    }

    public void register(Colleague colleague) {
        if (colleague != null && !explicitColleagues.contains(colleague)) explicitColleagues.add(colleague);
    }

    public void unregister(Colleague colleague) {
        explicitColleagues.remove(colleague);
    }

    public void setMode(ScreenMode newMode) {
        if (newMode == null || newMode == mode) return;
        mode = newMode;

        applying = true;
        applyFrameSize();

        SwingUtilities.invokeLater(() -> {
            double uiScale = (mode == ScreenMode.SMALL) ? 0.5 : 1.0;

            applyUiScale(uiScale);

            // ✅ TEXT PHASE 1: store full text BEFORE anything truncates (entering SMALL)
            if (uiRoot != null && mode == ScreenMode.SMALL) {
                captureFullTextTree(uiRoot);
            }

            // notify colleagues (some of them may do their own truncation)
            if (uiRoot != null) notifyTreeColleagues(uiRoot, mode, uiScale);
            for (Colleague c : explicitColleagues) if (c != null) c.onScreenModeChanged(mode, uiScale);

            // ✅ TEXT PHASE 2:
            // - if SMALL: apply ellipsis (after colleagues/layout)
            // - else: restore full text (leaving SMALL)
            if (uiRoot != null) {
                if (mode == ScreenMode.SMALL) applyCompactTextTree(uiRoot);
                else restoreFullTextTree(uiRoot);
            }

            applying = false;
        });
    }

    private void applyFrameSize() {
        SwingUtilities.invokeLater(() -> {
            switch (mode) {
                case FULLSCREEN -> frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

                case SMALL -> {
                    frame.setExtendedState(JFrame.NORMAL);
                    frame.setSize(Math.max(400, normalSize.width / 2), Math.max(300, normalSize.height / 2));
                    frame.setLocationRelativeTo(null);
                }

                case NORMAL -> {
                    frame.setExtendedState(JFrame.NORMAL);
                    frame.setSize(normalSize);
                    if (normalLocation != null) frame.setLocation(normalLocation);
                    else frame.setLocationRelativeTo(null);
                }
            }
        });
    }

    private void applyUiScale(double scale) {
        if (uiRoot == null) return;

        scaleFonts(uiRoot, scale);
        scaleComponentSizes(uiRoot, scale);
        fixJTreeMetrics(uiRoot);

        SwingUtilities.updateComponentTreeUI(frame);

        uiRoot.invalidate();
        uiRoot.doLayout();
        uiRoot.validate();

        frame.revalidate();
        frame.repaint();
    }

    private void scaleFonts(Component c, double scale) {
        if (c instanceof JComponent jc) {
            Object orig = jc.getClientProperty(ORIG_FONT_KEY);
            Font base = (orig instanceof Font) ? (Font) orig : jc.getFont();

            if (orig == null && base != null) {
                jc.putClientProperty(ORIG_FONT_KEY, base);
            }

            Font originalFont = (Font) jc.getClientProperty(ORIG_FONT_KEY);
            if (originalFont != null) {
                float newSize = (float) Math.max(8f, originalFont.getSize2D() * (float) scale);
                jc.setFont(originalFont.deriveFont(newSize));
            }
        }

        if (c instanceof Container ct) {
            for (Component child : ct.getComponents()) {
                scaleFonts(child, scale);
            }
        }
    }

    private void scaleComponentSizes(Component c, double scale) {
        if (c instanceof JComponent jc) {
            if (!(jc instanceof JMenuBar) && !(jc instanceof JToolBar) && !(jc instanceof JTabbedPane)) {

                storeOriginalSizesIfMissing(jc);

                Dimension pref0 = (Dimension) jc.getClientProperty(ORIG_PREF_KEY);
                Dimension min0  = (Dimension) jc.getClientProperty(ORIG_MIN_KEY);
                Dimension max0  = (Dimension) jc.getClientProperty(ORIG_MAX_KEY);

                if (scale == 1.0) {
                    jc.setPreferredSize(pref0);
                    jc.setMinimumSize(min0);
                    jc.setMaximumSize(max0);
                } else {
                    jc.setPreferredSize(scaleDim(pref0, scale));
                    jc.setMinimumSize(scaleDim(min0, scale));
                    jc.setMaximumSize(scaleDim(max0, scale));
                }
            }
        }

        if (c instanceof Container ct) {
            for (Component child : ct.getComponents()) {
                scaleComponentSizes(child, scale);
            }
        }
    }

    private void storeOriginalSizesIfMissing(JComponent jc) {
        if (jc.getClientProperty(ORIG_PREF_KEY) == null) jc.putClientProperty(ORIG_PREF_KEY, jc.getPreferredSize());
        if (jc.getClientProperty(ORIG_MIN_KEY) == null)  jc.putClientProperty(ORIG_MIN_KEY, jc.getMinimumSize());
        if (jc.getClientProperty(ORIG_MAX_KEY) == null)  jc.putClientProperty(ORIG_MAX_KEY, jc.getMaximumSize());
    }

    private Dimension scaleDim(Dimension d, double scale) {
        if (d == null) return null;
        if (d.width <= 0 && d.height <= 0) return d;
        int w = (int) Math.max(1, Math.round(d.width * scale));
        int h = (int) Math.max(1, Math.round(d.height * scale));
        return new Dimension(w, h);
    }

    private void fixJTreeMetrics(Component c) {
        if (c instanceof JTree tree) {
            Font f = tree.getFont();
            if (f != null) {
                FontMetrics fm = tree.getFontMetrics(f);
                int rh = Math.max(18, fm.getHeight() + 6);
                tree.setRowHeight(rh);
            }
            tree.revalidate();
            tree.repaint();
        }

        if (c instanceof Container ct) {
            for (Component child : ct.getComponents()) {
                fixJTreeMetrics(child);
            }
        }
    }

    private void notifyTreeColleagues(Component c, ScreenMode mode, double uiScale) {
        if (c instanceof Colleague col) {
            col.onScreenModeChanged(mode, uiScale);
        }

        if (c instanceof Container ct) {
            for (Component child : ct.getComponents()) {
                notifyTreeColleagues(child, mode, uiScale);
            }
        }
    }

    public void syncFromFrameState() {
        ScreenMode detected = detectCurrentMode();

        if (detected != mode) {
            mode = detected;

            SwingUtilities.invokeLater(() -> {
                double uiScale = (mode == ScreenMode.SMALL) ? 0.5 : 1.0;

                applyUiScale(uiScale);

                // ✅ same two-phase text logic here too
                if (uiRoot != null && mode == ScreenMode.SMALL) {
                    captureFullTextTree(uiRoot);
                }

                if (uiRoot != null) notifyTreeColleagues(uiRoot, mode, uiScale);
                for (Colleague c : explicitColleagues) {
                    if (c != null) c.onScreenModeChanged(mode, uiScale);
                }

                if (uiRoot != null) {
                    if (mode == ScreenMode.SMALL) applyCompactTextTree(uiRoot);
                    else restoreFullTextTree(uiRoot);
                }
            });
        }
    }

    private ScreenMode detectCurrentMode() {
        boolean maximized = (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
        if (maximized) return ScreenMode.FULLSCREEN;

        Dimension sz = frame.getSize();
        int targetW = Math.max(400, normalSize.width / 2);
        int targetH = Math.max(300, normalSize.height / 2);

        int tolW = Math.max(12, targetW / 20);
        int tolH = Math.max(12, targetH / 20);

        boolean smallLike =
                Math.abs(sz.width - targetW) <= tolW &&
                        Math.abs(sz.height - targetH) <= tolH;

        return smallLike ? ScreenMode.SMALL : ScreenMode.NORMAL;
    }

    // ============================================================
    // ✅ NEW: Text capture + compact + restore
    // ============================================================

    /** Store full text for labels/buttons BEFORE any truncation happens. */
    private void captureFullTextTree(Component c) {
        if (c instanceof JLabel lbl) captureFull(lbl);
        else if (c instanceof AbstractButton btn) captureFull(btn);

        if (c instanceof Container ct) {
            for (Component child : ct.getComponents()) {
                captureFullTextTree(child);
            }
        }
    }

    private void captureFull(JLabel lbl) {
        if (lbl == null) return;
        // Store only if missing; DO NOT overwrite once stored.
        if (lbl.getClientProperty(FULL_TEXT_KEY) == null) {
            lbl.putClientProperty(FULL_TEXT_KEY, safeText(lbl.getText()));
        }
    }

    private void captureFull(AbstractButton btn) {
        if (btn == null) return;
        if (btn.getClientProperty(FULL_TEXT_KEY) == null) {
            btn.putClientProperty(FULL_TEXT_KEY, safeText(btn.getText()));
        }
    }

    /** Apply ellipsis from the stored full text. */
    private void applyCompactTextTree(Component c) {
        if (c instanceof JLabel lbl) {
            String full = (String) lbl.getClientProperty(FULL_TEXT_KEY);
            if (full != null) lbl.setText(fitWithEllipsis(lbl, full));
        } else if (c instanceof AbstractButton btn) {
            String full = (String) btn.getClientProperty(FULL_TEXT_KEY);
            if (full != null) btn.setText(fitWithEllipsis(btn, full));
        }

        if (c instanceof Container ct) {
            for (Component child : ct.getComponents()) {
                applyCompactTextTree(child);
            }
        }
    }

    /** Restore exact full text when leaving SMALL. */
    private void restoreFullTextTree(Component c) {
        if (c instanceof JLabel lbl) {
            Object full = lbl.getClientProperty(FULL_TEXT_KEY);
            if (full instanceof String s) lbl.setText(s);
        } else if (c instanceof AbstractButton btn) {
            Object full = btn.getClientProperty(FULL_TEXT_KEY);
            if (full instanceof String s) btn.setText(s);
        }

        if (c instanceof Container ct) {
            for (Component child : ct.getComponents()) {
                restoreFullTextTree(child);
            }
        }
    }

    private static String safeText(String s) {
        return s == null ? "" : s;
    }

    private String fitWithEllipsis(JComponent jc, String fullText) {
        if (fullText == null) fullText = "";

        Font f = jc.getFont();
        if (f == null) return fullText;

        FontMetrics fm = jc.getFontMetrics(f);
        if (fm == null) return fullText;

        Insets ins = jc.getInsets();
        int width = jc.getWidth();
        if (width <= 0) {
            Dimension pref = jc.getPreferredSize();
            width = (pref != null) ? pref.width : 0;
        }
        int avail = Math.max(0, width - ins.left - ins.right);

        if (avail <= 0) return fullText;
        if (fm.stringWidth(fullText) <= avail) return fullText;

        final String ell = "...";
        int ellW = fm.stringWidth(ell);
        if (ellW >= avail) return ell;

        int lo = 0, hi = fullText.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            String sub = fullText.substring(0, mid);
            int w = fm.stringWidth(sub) + ellW;
            if (w <= avail) lo = mid;
            else hi = mid - 1;
        }

        return fullText.substring(0, Math.max(0, lo)) + ell;
    }
}
