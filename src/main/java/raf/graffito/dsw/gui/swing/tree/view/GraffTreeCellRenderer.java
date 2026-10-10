package raf.graffito.dsw.gui.swing.tree.view;

import raf.graffito.dsw.core.model.decorator.ColorDecorator;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Presentation;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.repository.implementation.Workspace;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.net.URL;

import static raf.graffito.dsw.core.model.decorator.Decorators.unwrap;

public class GraffTreeCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        // Guard: ensure we actually got our node type
        if (!(value instanceof GraffTreeItem gti)) {
            return this;
        }

        // Resolve decorated vs. core node
        GraffNode node = gti.getGraffNode();
        GraffNode core = (node != null) ? unwrap(node) : null;

        // --- Text: always use the REAL node name (don’t include decorator toString) ---
        if (core != null && core.getName() != null) {
            setText(core.getName());
        }

        // --- Icon by REAL type ---
        URL imageURL = null;
        if (core instanceof Workspace) {
            imageURL = getClass().getResource("/images/workspaceIcon.png");
        } else if (core instanceof Project) {
            imageURL = getClass().getResource("/images/projectIcon.png");
        } else if (core instanceof Presentation) {
            imageURL = getClass().getResource("/images/presIcon.png");
        } else if (core instanceof Slide) {
            imageURL = getClass().getResource("/images/slideIcon.png");
        }

        if (imageURL != null) {
            setIcon(new ImageIcon(imageURL));
        } else {
            setIcon(null);
        }

        // --- Optional: show a small color dot if decorated (purely visual) ---
        if (node instanceof ColorDecorator cd && cd.getColor() != null && getIcon() != null) {
            setIcon(new DotOverlayIcon(getIcon(), cd.getColor()));
        }

        return this;
    }

    /** Tiny colored dot overlay in the lower-right corner of the base icon. */
    private static final class DotOverlayIcon implements Icon {
        private final Icon base;
        private final Color color;

        private DotOverlayIcon(Icon base, Color color) {
            this.base = base;
            this.color = color;
        }

        @Override public int getIconWidth() { return base.getIconWidth(); }
        @Override public int getIconHeight() { return base.getIconHeight(); }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            base.paintIcon(c, g, x, y);
            int w = base.getIconWidth(), h = base.getIconHeight();
            int d = Math.max(6, Math.min(w, h) / 3);
            int dx = x + w - d, dy = y + h - d;
            g.setColor(color);
            g.fillOval(dx, dy, d, d);
            g.setColor(Color.DARK_GRAY);
            g.drawOval(dx, dy, d, d);
        }
    }
}
