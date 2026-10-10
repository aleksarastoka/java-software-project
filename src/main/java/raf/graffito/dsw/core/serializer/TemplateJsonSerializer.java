package raf.graffito.dsw.core.serializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.serializer.dto.ProjectDTO;
import raf.graffito.dsw.core.serializer.mapper.ProjectMapper;

import java.io.IOException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TemplateJsonSerializer {

    private final ObjectMapper mapper;
    private final Path templatesDir;

    public TemplateJsonSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.findAndRegisterModules();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        this.templatesDir = resolveTemplatesDir();
        ensureDir();
    }

    public Path getTemplatesDir() {
        return templatesDir;
    }

    /** Save current project as a template JSON under resources/templates/<name>.json */
    public Path saveTemplate(Project project, String templateName) throws Exception {
        if (project == null) throw new IllegalArgumentException("Project is null");
        String safe = sanitizeTemplateName(templateName);
        if (safe.isBlank()) throw new IllegalArgumentException("Template name is empty");

        Path out = templatesDir.resolve(safe + ".json");
        Files.createDirectories(templatesDir);

        ProjectDTO dto = ProjectMapper.toDTO(project);

        // Optional: ensure template DTO doesn't remember "disk path" via registry (if you do that elsewhere)
        // (This doesn't affect DTO; it just prevents weird UI logic if you accidentally set registry on save.)
        // ProjectFileRegistry.setPath(project, null);

        mapper.writeValue(out.toFile(), dto);
        return out;
    }

    /** Load a template by filename (without .json) and instantiate it as a Project */
    public Project loadTemplate(String templateName) throws Exception {
        String safe = sanitizeTemplateName(templateName);
        if (safe.isBlank()) throw new IllegalArgumentException("Template name is empty");

        Path file = templatesDir.resolve(safe + ".json");
        if (!Files.exists(file)) throw new IllegalArgumentException("Template does not exist: " + safe);

        ProjectDTO dto = mapper.readValue(file.toFile(), ProjectDTO.class);
        if (dto == null) throw new IllegalStateException("Failed to deserialize ProjectDTO");

        Project p = ProjectMapper.fromDTO(
                dto,
                ApplicationFramework.getInstance().getGraffRepoImplementation().getWorkspace()
        );

        // IMPORTANT: template should behave like a fresh project instance
        ProjectFileRegistry.setPath(p, null);

        return p;
    }

    /** List templates as base names (no extension). */
    public List<String> listTemplates() {
        try {
            if (!Files.exists(templatesDir)) return List.of();

            List<String> out = new ArrayList<>();
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(templatesDir, "*.json")) {
                for (Path p : ds) {
                    String name = p.getFileName().toString();
                    if (name.endsWith(".json")) name = name.substring(0, name.length() - 5);
                    out.add(name);
                }
            }
            out.sort(Comparator.naturalOrder());
            return out;
        } catch (IOException e) {
            return List.of();
        }
    }

    public boolean templateExists(String templateName) {
        String safe = sanitizeTemplateName(templateName);
        if (safe.isBlank()) return false;
        return Files.exists(templatesDir.resolve(safe + ".json"));
    }

    public boolean deleteTemplate(String templateName) throws IOException {
        String safe = sanitizeTemplateName(templateName);
        if (safe.isBlank()) return false;
        return Files.deleteIfExists(templatesDir.resolve(safe + ".json"));
    }

    // -------------------------
    // Helpers
    // -------------------------

    private void ensureDir() {
        try {
            Files.createDirectories(templatesDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create templates dir: " + templatesDir, e);
        }
    }

    /**
     * Requirement: store in local file in resources/templates.
     * In dev this is src/main/resources/templates.
     */
    private static Path resolveTemplatesDir() {
        Path cwd = Paths.get(System.getProperty("user.dir"));
        Path dev = cwd.resolve("src").resolve("main").resolve("resources").resolve("templates");
        if (Files.exists(dev.getParent())) return dev;
        return cwd.resolve("resources").resolve("templates");
    }

    private static String sanitizeTemplateName(String name) {
        if (name == null) return "";
        String n = name.trim();

        n = Normalizer.normalize(n, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "");

        n = n.replaceAll("[\\\\/:*?\"<>|]", "-");
        n = n.replaceAll("\\s+", " ");
        n = n.replaceAll("\\.+$", "");
        return n;
    }
}
