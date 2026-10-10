package raf.graffito.dsw.core.serializer;

import raf.graffito.dsw.core.model.repository.implementation.Project;

import java.nio.file.Path;
import java.util.Map;
import java.util.WeakHashMap;

public final class ProjectFileRegistry {

    /// Registar za fajlove koji omogucava da SaveAction odradi save bez potrebe da se zove jFileChooser ako je
    /// objekat vec jedanput sacuvan ili ucitan sa diska

    private static final Map<Project, Path> paths = new WeakHashMap<>();

    private ProjectFileRegistry() {}

    public static Path getPath(Project project) {
        return paths.get(project);
    }

    public static void setPath(Project project, Path path) {
        if (project == null) return;
        if (path == null) paths.remove(project);
        else paths.put(project, path);
    }

    public static boolean hasPath(Project project) {
        return getPath(project) != null;
    }
}
