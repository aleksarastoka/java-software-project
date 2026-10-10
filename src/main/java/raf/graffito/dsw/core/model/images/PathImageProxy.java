package raf.graffito.dsw.core.model.images;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;

public final class PathImageProxy implements ImageSource {

    private final Path path;

    private final ImageIcon thumbnailIcon;
    private final BufferedImage thumbnailBuffered; // fallback

    private volatile SoftReference<BufferedImage> fullCache = new SoftReference<>(null);

    private PathImageProxy(Path path, ImageIcon thumbnailIcon, BufferedImage thumbnailBuffered) {
        this.path = path;
        this.thumbnailIcon = thumbnailIcon;
        this.thumbnailBuffered = thumbnailBuffered;
    }

    public static PathImageProxy fromFile(Path path, int thumbMaxW, int thumbMaxH) throws IOException {
        Objects.requireNonNull(path, "path");
        if (!Files.exists(path)) throw new IOException("File does not exist: " + path);

        BufferedImage thumb = readThumbnailSubsampled(path, thumbMaxW, thumbMaxH);
        if (thumb == null) {
            BufferedImage full = ImageIO.read(path.toFile());
            if (full == null) throw new IOException("Unsupported image format: " + path);
            thumb = scaleToFit(full, thumbMaxW, thumbMaxH);
        }

        return new PathImageProxy(path, new ImageIcon(thumb), thumb);
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String getDisplayName() {
        Path fn = path.getFileName();
        return (fn != null) ? fn.toString() : path.toString();
    }

    @Override
    public ImageIcon getThumbnailIcon() {
        return thumbnailIcon;
    }

    @Override
    public BufferedImage loadFull() throws IOException {
        BufferedImage cached = fullCache.get();
        if (cached != null) return cached;

        synchronized (this) {
            cached = fullCache.get();
            if (cached != null) return cached;

            if (!Files.exists(path)) throw new IOException("Image file missing: " + path);

            BufferedImage full = ImageIO.read(path.toFile());
            if (full == null) throw new IOException("Unsupported image format: " + path);

            fullCache = new SoftReference<>(full);
            return full;
        }
    }

    public BufferedImage getThumbnailFallback() {
        return thumbnailBuffered;
    }

    private static BufferedImage readThumbnailSubsampled(Path path, int maxW, int maxH) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(path.toFile())) {
            if (in == null) return null;

            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) return null;

            ImageReader reader = readers.next();
            try {
                reader.setInput(in, true, true);

                int iw = reader.getWidth(0);
                int ih = reader.getHeight(0);
                if (iw <= 0 || ih <= 0) return null;

                double sx = iw / (double) maxW;
                double sy = ih / (double) maxH;
                int subsample = (int) Math.floor(Math.min(sx, sy));
                if (subsample < 1) subsample = 1;

                ImageReadParam param = reader.getDefaultReadParam();
                if (subsample > 1) param.setSourceSubsampling(subsample, subsample, 0, 0);

                BufferedImage reduced = reader.read(0, param);
                if (reduced == null) return null;

                if (reduced.getWidth() > maxW || reduced.getHeight() > maxH) {
                    return scaleToFit(reduced, maxW, maxH);
                }
                return reduced;
            } finally {
                reader.dispose();
            }
        }
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
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, w, h, null);
        } finally {
            g.dispose();
        }
        return out;
    }

    @Override
    public BufferedImage getFallbackImage() {
        return thumbnailBuffered;
    }

}
