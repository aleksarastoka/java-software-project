package raf.graffito.dsw.core.serializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.serializer.dto.ProjectDTO;
import raf.graffito.dsw.core.serializer.mapper.ProjectMapper;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ProjectJsonSerializer {

    /// Glavna klasa za serijalizaciju i deserijalizaciju Json fajlova

    private final ObjectMapper mapper;

    public ProjectJsonSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.findAndRegisterModules();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public void save(Project project, Path file) throws Exception {
        if (project == null) throw new IllegalArgumentException("Project is null");
        if (file == null) throw new IllegalArgumentException("File path is null");

        ProjectDTO dto = ProjectMapper.toDTO(project);

        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);

        mapper.writeValue(file.toFile(), dto);
    }

    public Project load(Path file) throws Exception {
        if (file == null) throw new IllegalArgumentException("File path is null");
        if (!Files.exists(file)) throw new IllegalArgumentException("File does not exist: " + file);
        if (!Files.isRegularFile(file)) throw new IllegalArgumentException("Not a file: " + file);

        ProjectDTO dto = mapper.readValue(file.toFile(), ProjectDTO.class);
        if (dto == null) throw new IllegalStateException("Failed to deserialize ProjectDTO");

        return ProjectMapper.fromDTO(
                dto,
                ApplicationFramework.getInstance().getGraffRepoImplementation().getWorkspace()
        );
    }
}
