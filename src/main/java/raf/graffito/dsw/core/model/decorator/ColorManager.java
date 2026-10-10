package raf.graffito.dsw.core.model.decorator;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;

import java.awt.Color;
import java.util.*;

public class ColorManager {

    private static final List<Color> allColors = List.of(
            Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.YELLOW,
            Color.PINK, Color.GRAY, new Color(128, 0, 128) // purple
            , new Color(135, 50, 50) // brown
            , new Color(0, 255, 255) // cyan
    );

    private static final Map<GraffNode, Set<Color>> usedByProject = new IdentityHashMap<>();

    public static List<Color> getAvailableColors(GraffNode projectCore) {
        if (projectCore == null) throw new IllegalArgumentException("projectCore must not be null");

        Set<Color> taken = usedByProject.get(projectCore);
        if (taken == null) taken = Collections.emptySet();

        List<Color> available = new ArrayList<>(allColors);
        available.removeAll(taken);
        return available;
    }
    public static void reserveColor(GraffNode projectCore, Color c) {
        if (projectCore == null || c == null) return;

        Set<Color> set = usedByProject.computeIfAbsent(projectCore, _ -> new HashSet<>());
        set.add(c);
    }

    public static void releaseColor(GraffNode projectCore, Color c) {
        if (projectCore == null || c == null) return;

        Set<Color> set = usedByProject.get(projectCore);
        if (set != null) {
            set.remove(c);
            if (set.isEmpty()) {
                usedByProject.remove(projectCore);
            }
        }
    }
}
