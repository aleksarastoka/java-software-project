// raf/graffito/dsw/gui/swing/color/ColorTags.java
package raf.graffito.dsw.gui.swing.color;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;

import java.awt.*;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ColorTags {
    private static final Map<GraffNode, Color> map = new IdentityHashMap<>();
    private ColorTags() {}

    public static void set(GraffNode core, Color color) {
        if (core == null) return;
        if (color == null) map.remove(core);
        else map.put(core, color);
    }

    public static Color get(GraffNode core) {
        return map.get(core);
    }

    public static void clear(GraffNode core) {
        map.remove(core);
    }
}
