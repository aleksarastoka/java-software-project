package raf.graffito.dsw.core.serializer.dto;

import java.util.ArrayList;
import java.util.List;

public class ProjectDTO {
    /// Objekat za prenos date Projekta

    // GraffNode.name
    public String name;

    // Project fields
    public String title;
    public String author;
    public int number;

    // Children
    public List<PresentationDTO> presentations = new ArrayList<>();
    public List<SlideDTO> slides = new ArrayList<>(); // slides directly under project (your model allows this)
}
