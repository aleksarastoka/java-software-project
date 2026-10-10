package raf.graffito.dsw.core.serializer.dto;

import java.util.ArrayList;
import java.util.List;

public class PresentationDTO {
    /// Objekat za prenos date Presentacije

    // GraffNode.name
    public String name;

    // keep for future-proofing (even if they are constants now)
    public int slideWidth;
    public int slideHeight;
    public int colorArgb; // ARGB, e.g. color.getRGB()


    public List<SlideDTO> slides = new ArrayList<>();
}
