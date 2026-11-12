package fr.tp.parking.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

/** Saves a PNG of the window (used to make the previews). */
final class Snapshot {
    private Snapshot() { }

    static void save(Scene scene, String path) {
        WritableImage image = scene.snapshot(null);
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        try {
            ImageIO.write(out, "png", new File(path));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
