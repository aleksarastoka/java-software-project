package raf.graffito.dsw.core.serializer.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "kind"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextBoxElementDTO.class, name = "TEXTBOX"),
        @JsonSubTypes.Type(value = ImageElementDTO.class,   name = "IMAGE"),
        @JsonSubTypes.Type(value = LogoElementDTO.class,    name = "LOGO")
})
public abstract class SlideElementDTO {
    /// Abstraktna klasa za prenos date SlideElement objekata, svaka DTO za SlideELement klasa nasledjuje ovu klasu
    // SlideElement fields
    public int x;
    public int y;

    public int width;
    public int height;

    public int rotateRatio; // matches your int rotateRatio
}
