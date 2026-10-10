package raf.graffito.dsw.core.serializer.mapper;

import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Presentation;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.core.model.repository.implementation.Slide;
import raf.graffito.dsw.core.model.structures.ImageElement;
import raf.graffito.dsw.core.model.structures.LogoElement;
import raf.graffito.dsw.core.model.structures.SlideElement;
import raf.graffito.dsw.core.model.structures.TextBoxElement;
import raf.graffito.dsw.core.serializer.dto.*;
import raf.graffito.dsw.gui.swing.color.ColorTags;

import raf.graffito.dsw.core.model.factory.slideElement.SlideElementFactory;
import raf.graffito.dsw.core.model.factory.slideElement.SlideElementType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static raf.graffito.dsw.core.model.decorator.Decorators.unwrap;

public final class ProjectMapper {

    private ProjectMapper() {}

    // ✅ use your registry-style factory for slide elements
    private static final SlideElementFactory SLIDE_ELEMENT_FACTORY = new SlideElementFactory();

    // =========================
    // Project → ProjectDTO
    // =========================
    public static ProjectDTO toDTO(Project project) {
        if (project == null) return null;

        ProjectDTO dto = new ProjectDTO();
        dto.name = project.getName();
        dto.title = project.getTitle();
        dto.author = project.getAuthor();
        dto.number = project.getNumber();

        for (GraffNode child : project.getChildren()) {
            GraffNode u = safeUnwrap(child);

            if (u instanceof Presentation pres) {
                dto.presentations.add(toDTO(pres));
            } else if (u instanceof Slide slide) {
                dto.slides.add(toDTO(slide));
            }
        }

        return dto;
    }

    // =========================
    // Presentation → PresentationDTO
    // =========================
    private static PresentationDTO toDTO(Presentation pres) {
        PresentationDTO dto = new PresentationDTO();
        dto.name = pres.getName();
        dto.slideWidth = pres.getSlideWidth();
        dto.slideHeight = pres.getSlideHeight();

        Color c = ColorTags.get(pres);
        dto.colorArgb = (c != null) ? c.getRGB() : new Color(60, 140, 255).getRGB();

        for (GraffNode child : pres.getChildren()) {
            GraffNode u = safeUnwrap(child);
            if (u instanceof Slide slide) dto.slides.add(toDTO(slide));
        }
        return dto;
    }

    // =========================
    // Slide → SlideDTO
    // =========================
    private static SlideDTO toDTO(Slide slide) {
        SlideDTO dto = new SlideDTO();
        dto.name = slide.getName();

        for (GraffNode child : slide.getChildren()) {
            GraffNode u = safeUnwrap(child);
            if (u instanceof SlideElement el) {
                dto.elements.add(toDTO(el));
            }
        }

        return dto;
    }

    // =========================
    // SlideElement → SlideElementDTO (polymorphic)
    // =========================
    private static SlideElementDTO toDTO(SlideElement el) {
        if (el instanceof TextBoxElement tb) {
            TextBoxElementDTO dto = new TextBoxElementDTO();
            fillBase(dto, tb);

            dto.baseFontSize = tb.getBaseFontSize();

            Dimension base = tb.getBaseBoxSize();
            dto.baseBoxWidth = (base != null) ? base.width : tb.getSize().width;
            dto.baseBoxHeight = (base != null) ? base.height : tb.getSize().height;

            dto.text = tb.getText();
            dto.fontFamily = tb.getFontFamily();
            dto.fontStyle = tb.getFontStyle();
            dto.baseFontSize = tb.getBaseFontSize();

            Color border = tb.getBorderColor();
            Color text = tb.getTextColor();
            dto.borderColorArgb = (border != null) ? border.getRGB() : new Color(60, 140, 255).getRGB();
            dto.textColorArgb = (text != null) ? text.getRGB() : Color.BLACK.getRGB();

            return dto;
        }

        if (el instanceof ImageElement img) {
            ImageElementDTO dto = new ImageElementDTO();
            fillBase(dto, img);
            dto.pngBase64 = bufferedImageToPngBase64(img.getImage());
            return dto;
        }

        if (el instanceof LogoElement logo) {
            LogoElementDTO dto = new LogoElementDTO();
            fillBase(dto, logo);

            Color c = logo.getColor();
            dto.colorArgb = (c != null) ? c.getRGB() : new Color(20, 20, 20).getRGB();
            dto.stroke = logo.getStroke();

            return dto;
        }

        throw new IllegalArgumentException("Unsupported SlideElement: " + el.getClass().getName());
    }

    // =========================
    // Helpers
    // =========================
    private static void fillBase(SlideElementDTO dto, SlideElement el) {
        Point p = el.getPos();
        Dimension s = el.getSize();

        dto.x = (p != null) ? p.x : 0;
        dto.y = (p != null) ? p.y : 0;

        dto.width = (s != null) ? s.width : 0;
        dto.height = (s != null) ? s.height : 0;

        dto.rotateRatio = el.getRotateRatio();
    }

    private static String bufferedImageToPngBase64(BufferedImage img) {
        if (img == null) return null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    private static GraffNode safeUnwrap(GraffNode node) {
        if (node == null) return null;
        try {
            return unwrap(node);
        } catch (Throwable t) {
            return node;
        }
    }

    // =========================
    // ProjectDTO → Project
    // =========================
    public static Project fromDTO(ProjectDTO dto, GraffNode parent) {
        if (dto == null) return null;

        Project project = new Project(
                parent,
                dto.title,
                dto.author,
                dto.name
        );
        project.setNumber(dto.number);

        if (dto.presentations != null) {
            for (PresentationDTO pDto : dto.presentations) {
                if (pDto == null) continue;
                fromDTO(pDto, project);
            }
        }

        if (dto.slides != null) {
            for (SlideDTO sDto : dto.slides) {
                if (sDto == null) continue;
                Slide slide = fromDTO(sDto, project);
                project.addChild(slide);
            }
        }

        return project;
    }

    // =========================
    // PresentationDTO → Presentation
    // =========================
    private static Presentation fromDTO(PresentationDTO dto, Project parent) {
        Presentation pres = new Presentation(parent, dto.name);

        ColorTags.set(pres, new Color(dto.colorArgb, true));

        if (dto.slides != null) {
            for (SlideDTO sDto : dto.slides) {
                if (sDto == null) continue;
                Slide slide = fromDTO(sDto, pres);
                pres.addChild(slide);
            }
        }
        return pres;
    }

    // =========================
    // SlideDTO → Slide
    // =========================
    private static Slide fromDTO(SlideDTO dto, GraffNode parent) {
        Slide slide = new Slide(dto.name, parent);

        if (dto.elements != null) {
            for (SlideElementDTO eDto : dto.elements) {
                if (eDto == null) continue;

                SlideElement el = fromDTO(eDto);
                if (el != null) {
                    slide.addChild(el);
                }
            }
        }

        return slide;
    }

    // =========================
    // SlideElementDTO → SlideElement (polymorphic) ✅ NOW VIA FACTORIES
    // =========================
    private static SlideElement fromDTO(SlideElementDTO dto) {

        if (dto instanceof TextBoxElementDTO tb) {
            // base size (used for baseBoxSize semantics)
            Dimension base = new Dimension(
                    Math.max(1, tb.baseBoxWidth),
                    Math.max(1, tb.baseBoxHeight)
            );

            // actual current size (after user resizing)
            Dimension actual = new Dimension(
                    Math.max(1, tb.width),
                    Math.max(1, tb.height)
            );

            SlideElement created = SLIDE_ELEMENT_FACTORY.create(
                    SlideElementType.TEXT_BOX,
                    new Point(tb.x, tb.y),
                    base,
                    tb.rotateRatio,
                    null
            );

            if (!(created instanceof TextBoxElement el)) {
                throw new IllegalStateException("Factory returned non-TextBoxElement for TEXT_BOX: " + created.getClass());
            }

            // restore actual size
            el.setSize(actual);

            // restore textbox properties
            el.setText(tb.text != null ? tb.text : "");
            if (tb.fontFamily != null) el.setFontFamily(tb.fontFamily);
            el.setFontStyle(tb.fontStyle);
            el.setBaseFontSize(tb.baseFontSize);

            el.setBorderColor(new Color(tb.borderColorArgb, true));
            el.setTextColor(new Color(tb.textColorArgb, true));

            return el;
        }

        if (dto instanceof ImageElementDTO img) {
            BufferedImage bi = pngBase64ToBufferedImage(img.pngBase64);

            SlideElement created = SLIDE_ELEMENT_FACTORY.create(
                    SlideElementType.IMAGE,
                    new Point(img.x, img.y),
                    new Dimension(Math.max(1, img.width), Math.max(1, img.height)),
                    img.rotateRatio,
                    bi
            );

            if (!(created instanceof ImageElement)) {
                throw new IllegalStateException("Factory returned non-ImageElement for IMAGE: " + created.getClass());
            }

            return created;
        }

        if (dto instanceof LogoElementDTO logo) {
            SlideElement created = SLIDE_ELEMENT_FACTORY.create(
                    SlideElementType.LOGO,
                    new Point(logo.x, logo.y),
                    new Dimension(Math.max(1, logo.width), Math.max(1, logo.height)),
                    logo.rotateRatio,
                    null
            );

            if (!(created instanceof LogoElement el)) {
                throw new IllegalStateException("Factory returned non-LogoElement for LOGO: " + created.getClass());
            }

            el.setColor(new Color(logo.colorArgb, true));
            el.setStroke(logo.stroke);

            return el;
        }

        throw new IllegalArgumentException("Unsupported SlideElementDTO: " + dto.getClass().getName());
    }

    private static BufferedImage pngBase64ToBufferedImage(String base64) {
        if (base64 == null || base64.isBlank()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return ImageIO.read(new java.io.ByteArrayInputStream(bytes));
        } catch (Exception e) {
            return null;
        }
    }
}
