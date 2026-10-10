package raf.graffito.dsw.core.serializer.dto;

import java.util.ArrayList;
import java.util.List;

public class SlideDTO {

    /// Objekat za prenos date Slajdova

    // GraffNode.name
    public String name;

    public List<SlideElementDTO> elements = new ArrayList<>();
}
