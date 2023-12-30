package dev.justix.gtavtools.util;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class ImageUtil {

    private static final Logger LOGGER;

    static {
        LOGGER = new Logger(System.out);
    }

    public static void debug(String name, int[][] pixels) {
        if (!SystemUtil.DEBUG) return;

        BufferedImage image = new BufferedImage(pixels[0].length, pixels.length, BufferedImage.TYPE_BYTE_BINARY);
        image.getRaster().setPixels(0, 0, pixels[0].length, pixels.length, Stream.of(pixels).flatMapToInt(Arrays::stream).toArray());

        debug(name, image);
    }

    public static void debug(String name, BufferedImage image) {
        try {
            ImageIO.write(
                    image,
                    "PNG",
                    new File("F:\\pictures\\screenshots", name + ".png")
            );
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not save debug image: " + ex.getMessage());
        }
    }

    public static BufferedImage fromResource(String path) throws IOException {
        return ImageIO.read(Objects.requireNonNull(ImageUtil.class.getResource("/" + SystemUtil.RESOLUTION + path)));
    }

    public static BufferedImage transform(BufferedImage image, boolean binary) {
        return transform(image, 0, 0, image.getWidth(), image.getHeight(), binary);
    }

    public static BufferedImage transform(BufferedImage image,Rectangle rectangle, boolean binary) {
        return transform(image, rectangle.x, rectangle.y, rectangle.width, rectangle.height, binary);
    }

    public static BufferedImage transform(BufferedImage image,
                                          int x, int y,
                                          int width, int height,
                                          boolean binary) {
        BufferedImage resizedImage = new BufferedImage(
                width,
                height,
                binary ? BufferedImage.TYPE_BYTE_BINARY : BufferedImage.TYPE_INT_RGB
        );
        Graphics2D resizedImageGraphics = resizedImage.createGraphics();
        resizedImageGraphics.drawImage(image, x, y, width, height, null);
        resizedImageGraphics.dispose();

        return resizedImage;
    }

    public static BufferedImage crop(BufferedImage image, Rectangle rectangle) {
        return crop(image, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public static BufferedImage crop(BufferedImage image, int x, int y, int width, int height) {
        BufferedImage croppedImage = new BufferedImage(width, height, image.getType());
        Graphics2D resizedImageGraphics = croppedImage.createGraphics();
        resizedImageGraphics.drawImage(
                image,
                0, 0, width, height,
                Math.max(x, 0), Math.max(y, 0), x + Math.min(image.getWidth() - x, width), y + Math.min(image.getHeight() - y, height),
                null
        );
        resizedImageGraphics.dispose();

        return croppedImage;
    }

    public static int[][] getPixels(BufferedImage image) {
        final int width = image.getWidth(), height = image.getHeight();
        final int[][] pixels = new int[height][width];
        final Raster raster = image.getData();
        final int[] pixel = new int[4];

        for (int y = 0; y < height; y++) {
            final int[] row = pixels[y];

            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixel);

                row[x] = pixel[0];
            }
        }

        return pixels;
    }

    public static double getMaxMatchPercentage(BufferedImage image1, BufferedImage image2, float maxRequiredPercentage, int maxXOffset, int maxYOffset) {
        return getMaxMatchPercentage(getPixels(image1), getPixels(image2), maxRequiredPercentage, maxXOffset, maxYOffset, true);
    }

    public static double getMaxMatchPercentage(int[][] image1Pixels, int[][] image2Pixels, float maxRequiredPercentage, int maxXOffset, int maxYOffset, boolean checkBlack) {
        final AtomicReference<Double> matchPercentage = new AtomicReference<>(0.0);
        final CountDownLatch latch = new CountDownLatch(Math.max(1, maxXOffset * 2 + 1) * Math.max(1, maxYOffset * 2 + 1));

        try (ExecutorService executor = Executors.newFixedThreadPool(16)) {
            int xOffset = -maxXOffset, yOffset = -maxYOffset;

            do {
                int finalXOffset = xOffset, finalYOffset = yOffset;

                executor.submit(() -> {
                    try {
                        if (matchPercentage.get() >= (maxRequiredPercentage / 100.0))
                            return;

                        double currentMatchPercentage = compare(image1Pixels, image2Pixels, finalXOffset, finalYOffset, checkBlack);

                        if (currentMatchPercentage > matchPercentage.get())
                            matchPercentage.set(currentMatchPercentage);
                    } finally {
                        latch.countDown();
                    }
                });

                if (++xOffset > maxXOffset) {
                    xOffset = -maxXOffset;
                    yOffset++;
                }
            } while (yOffset <= maxYOffset);
        }

        try {
            latch.await();
        } catch (InterruptedException ignore) {
        }

        return matchPercentage.get();
    }

    public static double compare(int[][] image1Colors, int[][] image2Colors, int xOffset, int yOffset, boolean checkBlack) {
        final int image1Height = image1Colors.length, image1Width = image1Colors[0].length;
        final int image2Height = image2Colors.length, image2Width = image2Colors[0].length;
        int checked = 0, matches = 0;

        if (image2Height - yOffset <= 0 || image2Width - xOffset <= 0)
            return 0d;

        for (int y = Math.max(-yOffset, 0); y < Math.min(image1Height, Math.min(image2Height, image2Height - yOffset)); y++) {
            final int[] yColors1 = image1Colors[y], yColors2 = image2Colors[y + yOffset];

            for (int x = Math.max(-xOffset, 0); x < Math.min(image1Width, Math.min(image2Width, image2Width - xOffset)); x++) {
                final int pixelColor = yColors1[x];

                if (!checkBlack && pixelColor == 0)
                    continue;

                if (pixelColor == yColors2[x + xOffset])
                    matches++;

                checked++;
            }
        }

        return (double) matches / checked;
    }

}
