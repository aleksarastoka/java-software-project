package raf.graffito.dsw.core.model.images;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public interface ImageSource {
    String getDisplayName();
    ImageIcon getThumbnailIcon();
    BufferedImage loadFull() throws IOException;

    // ✅ add this (compile fix)
    default BufferedImage getFallbackImage() {
        return null;
    }
}
