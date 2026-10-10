package raf.graffito.dsw.gui.swing.views.editor;

import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.composite.GraffNodeComposite;
import raf.graffito.dsw.core.model.repository.implementation.Presentation;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.strategy.OccupancyReport;
import raf.graffito.dsw.gui.swing.color.ColorTags;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.List;

import static raf.graffito.dsw.core.model.decorator.Decorators.unwrap;

public class SlidesTabbedView extends JTabbedPane {

    private final ProjectView owner; // da osvezimo sidebar + undo/redo

    public SlidesTabbedView(Project project, ProjectView owner) {
        this.owner = owner;

        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        buildTabs(project);

        // init active slide
        var mgr = ApplicationFramework.getInstance().getToolStateManager();
        if (mgr != null) mgr.setActiveSlide(getActiveSlide());

        owner.getRightSidebarPanel().setEnabledForSlide(hasRealSlideSelected());
        owner.updateUndoRedoState();

        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                var mgr = ApplicationFramework.getInstance().getToolStateManager();
                if (mgr != null) mgr.setActiveSlide(getActiveSlide());

                owner.getRightSidebarPanel().setEnabledForSlide(hasRealSlideSelected());
                owner.updateUndoRedoState();

                // ✅ refit active slide on tab switch (so it matches current screen size)
                SlideView sv = getActiveSlideView();
                if (sv != null) SwingUtilities.invokeLater(sv::onScreenSizeChanged);

                Component c = getSelectedComponent();
                if (c != null) c.repaint();
            }
        });
    }

    public void refreshActiveTabHeader() {
        Component c = getSelectedComponent();
        if (c instanceof SlideTabPanel stp) stp.refreshTitle();
    }

    public Slide getActiveSlide() {
        Component c = getSelectedComponent();
        if (c instanceof SlideTabPanel stp) return stp.slide;
        return null;
    }

    public SlideView getActiveSlideView() {
        Component c = getSelectedComponent();
        if (c instanceof SlideTabPanel stp) return stp.slideView;
        return null;
    }

    public boolean hasRealSlideSelected() {
        return (getSelectedComponent() instanceof SlideTabPanel);
    }

    // ===== tab panel =====
    private static class SlideTabPanel extends JPanel {
        Presentation presOrNull;
        final Slide slide;
        final JLabel headerLabel;
        final SlideView slideView;

        SlideTabPanel(Presentation pres, Slide slide) {
            super(new BorderLayout());
            this.presOrNull = pres;
            this.slide = slide;

            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            headerLabel = new JLabel("", SwingConstants.LEFT);
            headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            add(headerLabel, BorderLayout.NORTH);

            slideView = new SlideView(slide);
            add(slideView, BorderLayout.CENTER);

            // ✅ register with global screen mediator
            MainFrame.getInstance().getScreenSizeMediator().register(slideView);

            // ✅ fit once the component has real size
            SwingUtilities.invokeLater(slideView::onScreenSizeChanged);

            refreshTitle();
        }

//        void refreshTitle() {
//            String presLabel = (presOrNull != null && presOrNull.getName() != null) ? presOrNull.getName() : "—";
//            String slideLabel = (slide.getName() != null) ? slide.getName() : "Slide";
//            headerLabel.setText("<html><b>Presentation:</b> " + presLabel +
//                    " &nbsp;&nbsp; <b>Slide:</b> " + slideLabel + "</html>");
//        }
        void refreshTitle() {
            String presLabel = (presOrNull != null && presOrNull.getName() != null) ? presOrNull.getName() : "—";
            String slideLabel = (slide.getName() != null) ? slide.getName() : "Slide";

            int occP = OccupancyReport.matrixPercent(slide); // 0..100

            headerLabel.setText(
                    "<html><b>Presentation:</b> " + presLabel +
                            " &nbsp;&nbsp; <b>Slide:</b> " + slideLabel +
                            " &nbsp;&nbsp; <b>Occupancy:</b> " + occP + "%</html>"
            );
        }
        @Override
        public void removeNotify() {
            super.removeNotify();

            // keep your existing subscriber cleanup
            slide.removeSubscriber(slideView); // potencijalno ne treba

            // ✅ unregister from mediator (avoid leaks)
            MainFrame.getInstance().getScreenSizeMediator().unregister(slideView);
        }
    }

    // ===== build =====
    private void buildTabs(Project project) {
        List<GraffNode> children = ((GraffNodeComposite) project).getChildren();
        int count = 0;

        if (children != null) {
            for (GraffNode child : children) {
                GraffNode core = unwrap(child);

                if (core instanceof Presentation pres) {
                    Color presColor = ColorTags.get(pres);
                    List<GraffNode> slides = ((GraffNodeComposite) pres).getChildren();
                    if (slides == null) continue;

                    for (GraffNode slideNode : slides) {
                        GraffNode sc = unwrap(slideNode);
                        if (!(sc instanceof Slide slide)) continue;

                        String tabTitle = buildTabTitle(pres.getName(), slide.getName(), ++count);
                        addSlideTabInternal(pres, slide, tabTitle, presColor);
                    }
                    continue;
                }

                if (core instanceof Slide slide) {
                    String slideName = slide.getName();
                    String tabTitle = (slideName != null && !slideName.isBlank())
                            ? ("Slide — " + slideName)
                            : ("Slide " + (++count));

                    Color projectColor = ColorTags.get(project);
                    addSlideTabInternal(null, slide, tabTitle, projectColor);
                }
            }
        }

        if (getTabCount() == 0) {
            addTab("No slides", createEmptyPanel());
            setEnabledAt(0, false);
            setSelectedIndex(0);
        } else {
            setSelectedIndex(0);

            // ✅ after initial build, fit the first real slide view
            SlideView sv = getActiveSlideView();
            if (sv != null) SwingUtilities.invokeLater(sv::onScreenSizeChanged);
        }
    }

    private void addSlideTabInternal(Presentation presOrNull, Slide slide, String tabTitle, Color tabColor) {
        SlideTabPanel panel = new SlideTabPanel(presOrNull, slide);
        addTab(tabTitle, panel);
        int index = getTabCount() - 1;
        if (tabColor != null) {
            setBackgroundAt(index, tabColor);
            setForegroundAt(index, bestContrast(tabColor));
        }
    }

    // ===== Public helpers (used by ProjectView) =====

    public void addSlideTab(Presentation presOrNull, Slide slide, Color tabColor, int indexIgnored) {
        removeEmptyPlaceholderIfPresent();

        String title = (presOrNull != null)
                ? buildTabTitle(presOrNull.getName(), slide.getName(), getTabCount() + 1)
                : ("Slide — " + (slide.getName() == null ? (getTabCount() + 1) : slide.getName()));

        addSlideTabInternal(presOrNull, slide, title, tabColor);

        // auto-select new tab
        setSelectedIndex(getTabCount() - 1);

        // sync active slide + sidebar + undo/redo
        var mgr = ApplicationFramework.getInstance().getToolStateManager();
        if (mgr != null) mgr.setActiveSlide(getActiveSlide());
        owner.getRightSidebarPanel().setEnabledForSlide(hasRealSlideSelected());
        owner.updateUndoRedoState();

        // ✅ fit the just-created tab’s slide (safe even if already done)
        SlideView sv = getActiveSlideView();
        if (sv != null) SwingUtilities.invokeLater(sv::onScreenSizeChanged);
    }

    public void renameSlideTab(Slide slide) {
        int idx = findTabIndexBySlide(slide);
        if (idx < 0) return;

        Component c = getComponentAt(idx);
        Presentation p = (c instanceof SlideTabPanel stp) ? stp.presOrNull : null;

        String title = (p != null)
                ? buildTabTitle(p.getName(), slide.getName(), idx + 1)
                : ("Slide — " + (slide.getName() == null ? (idx + 1) : slide.getName()));

        setTitleAt(idx, title);
        if (c instanceof SlideTabPanel stp) stp.refreshTitle();
    }

    public void renamePresentationTabs(Presentation pres) {
        for (int i = 0; i < getTabCount(); i++) {
            Component c = getComponentAt(i);
            if (!(c instanceof SlideTabPanel stp)) continue;
            if (stp.presOrNull == pres) {
                String title = buildTabTitle(pres.getName(), stp.slide.getName(), i + 1);
                setTitleAt(i, title);
                stp.refreshTitle();
            }
        }
    }

    public void removeEmptyPlaceholderIfPresent() {
        if (getTabCount() == 1 && "No slides".equals(getTitleAt(0))) {
            removeTabAt(0);
        }
    }

    // ===== internals =====

    private int findTabIndexBySlide(Slide target) {
        for (int i = 0; i < getTabCount(); i++) {
            Component c = getComponentAt(i);
            if (c instanceof SlideTabPanel stp && stp.slide == target) return i;
        }
        return -1;
    }

    private static String buildTabTitle(String presName, String slideName, int index) {
        String p = (presName != null) ? presName : "Presentation";
        String s = (slideName != null) ? slideName : ("Slide " + index);
        return p + " — " + s;
    }

    private static JComponent createEmptyPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("This project has no slides.", SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }

    private static Color bestContrast(Color bg) {
        double luminance = (0.2126 * bg.getRed() + 0.7152 * bg.getGreen() + 0.0722 * bg.getBlue()) / 255.0;
        return (luminance < 0.6) ? Color.WHITE : Color.BLACK;
    }
}
