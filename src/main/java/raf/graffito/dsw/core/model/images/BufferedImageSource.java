package raf.graffito.dsw.core.model.images;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public final class BufferedImageSource implements ImageSource {

    private final String displayName;
    private final BufferedImage image;
    private final ImageIcon thumbnail;

    public BufferedImageSource(String displayName, BufferedImage image, int thumbMaxW, int thumbMaxH) {
        this.displayName = (displayName == null || displayName.isBlank()) ? "Image" : displayName;
        this.image = Objects.requireNonNull(image, "image");
        this.thumbnail = new ImageIcon(scaleToFit(image, thumbMaxW, thumbMaxH));
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public ImageIcon getThumbnailIcon() {
        return thumbnail;
    }

    @Override
    public BufferedImage loadFull() throws IOException {
        return image; // already full-res
    }

    private static BufferedImage scaleToFit(BufferedImage src, int maxW, int maxH) {
        double sx = maxW / (double) src.getWidth();
        double sy = maxH / (double) src.getHeight();
        double s = Math.min(sx, sy);

        int w = Math.max(1, (int) Math.round(src.getWidth() * s));
        int h = Math.max(1, (int) Math.round(src.getHeight() * s));

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, w, h, null);
        } finally {
            g.dispose();
        }
        return out;
    }
}
