package raf.graffito.dsw.core.serializer.dto;

public class TextBoxElementDTO extends SlideElementDTO {

    /// Objekat za prenos date TextBoxElementa

    public String text;

    public String fontFamily;
    public int fontStyle;      // matches java.awt.Font style int

    public int baseFontSize;
    public int baseBoxWidth;
    public int baseBoxHeight;


    // java.awt.Color is not JSON-friendly by default; store ARGB int
    public int borderColorArgb;
    public int textColorArgb;
}
