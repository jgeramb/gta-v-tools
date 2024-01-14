package dev.justix.gtavtools.util.images;

import dev.justix.gtavtools.config.ApplicationConfig;
import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.util.SystemUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        if (!SystemUtil.DEBUG) return;

        try {
            ImageIO.write(
                    image,
                    "PNG",
                    new File(ApplicationConfig.CONFIG.getString("debugImageDirectory"), name + ".png")
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

    public static BufferedImage transform(BufferedImage image, Rectangle rectangle, boolean binary) {
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

    public static BufferedImage fromPixels(int[][] pixels) {
        BufferedImage image = new BufferedImage(pixels[0].length, pixels.length, BufferedImage.TYPE_BYTE_BINARY);
        image.getRaster().setPixels(
                0,
                0,
                pixels[0].length,
                pixels.length,
                Stream.of(pixels).flatMapToInt(Arrays::stream).toArray()
        );

        return image;
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
                Math.max(x, 0),
                Math.max(y, 0),
                x + Math.min(image.getWidth() - x, width),
                y + Math.min(image.getHeight() - y, height),
                null
        );
        resizedImageGraphics.dispose();

        return croppedImage;
    }

    public static int[][] getPixels(BufferedImage image) {
        return getPixels(image, 0, 255, 0, 255, 0, 255, true);
    }

    public static int[][] getPixels(BufferedImage image,
                                    int minRed, int maxRed,
                                    int minGreen, int maxGreen,
                                    int minBlue, int maxBlue,
                                    boolean isBinary) {
        final int width = image.getWidth(), height = image.getHeight();
        final int[][] pixels = new int[height][width];
        final Raster raster = image.getData();
        final int[] pixel = new int[4];

        for (int y = 0; y < height; y++) {
            final int[] row = pixels[y];

            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixel);

                if(isBinary)
                    row[x] = pixel[0];
                else {
                    boolean red = pixel[0] >= minRed && pixel[0] <= maxRed,
                            green = pixel[1] >= minGreen && pixel[1] <= maxGreen,
                            blue = pixel[2] >= minBlue && pixel[2] <= maxBlue;

                    row[x] = (red && green && blue) ? 1 : 0;
                }
            }
        }

        return pixels;
    }

    public static double getBestMatchPercentage(BufferedImage image1, BufferedImage image2,
                                                int maxXOffset, int maxYOffset) {
        return getBestMatchPercentage(
                getPixels(image1), getPixels(image2),
                maxXOffset,
                maxYOffset
        );
    }

    public static double getBestMatchPercentage(int[][] image1Pixels, int[][] image2Pixels,
                                                int maxXOffset, int maxYOffset) {
        return getBestMatchPercentage(
                image1Pixels, image2Pixels,
                -maxXOffset, maxXOffset,
                -maxYOffset, maxYOffset,
                false
        );
    }

    public static double getBestMatchPercentage(int[][] image1Pixels, int[][] image2Pixels,
                                                int minXOffset, int maxXOffset,
                                                int minYOffset, int maxYOffset,
                                                boolean checkImage2WhitePixelsOnly) {
        final double requiredCheckedPixels = Stream.of(image1Pixels).flatMapToInt(Arrays::stream).filter(pixel -> pixel == 1).count() * 0.45;
        final Set<ComparisonResult> results = new HashSet<>();
        final int resultCount = (maxXOffset - minXOffset + 1) * (maxYOffset - minYOffset + 1);
        final CountDownLatch latch = new CountDownLatch(resultCount);

        try (ExecutorService executor = Executors.newFixedThreadPool(16)) {
            int xOffset = minXOffset, yOffset = minYOffset;

            do {
                int finalXOffset = xOffset, finalYOffset = yOffset;

                executor.submit(() -> {
                    try {
                        final ComparisonResult result = compare(image1Pixels, image2Pixels, finalXOffset, finalYOffset, checkImage2WhitePixelsOnly);

                        if (result.checked() > requiredCheckedPixels && result.matches() > 0)
                            results.add(result);
                    } finally {
                        latch.countDown();
                    }
                });

                if (++xOffset > maxXOffset) {
                    xOffset = minXOffset;
                    yOffset++;
                }
            } while (yOffset <= maxYOffset);
        }

        try {
            latch.await();
        } catch (InterruptedException ignore) {
        }

        return results
                .stream()
                .mapToDouble(ComparisonResult::getPercentage)
                .max()
                .orElse(0d);
    }

    public static ComparisonResult compare(int[][] image1Colors, int[][] image2Colors, int xOffset, int yOffset, boolean checkImage2WhitePixelsOnly) {
        final int image1Height = image1Colors.length, image1Width = image1Colors[0].length;
        final int image2Height = image2Colors.length, image2Width = image2Colors[0].length;
        int checked = 0, matches = 0;

        if (image2Width - Math.abs(xOffset) <= 0 || image2Height - Math.abs(yOffset) <= 0)
            return new ComparisonResult(0, 0);

        for (int y = Math.max(-yOffset, 0); y < Math.min(image1Height, image2Height - yOffset); y++) {
            final int[] yColors1 = image1Colors[y], yColors2 = image2Colors[y + yOffset];

            for (int x = Math.max(-xOffset, 0); x < Math.min(image1Width, image2Width - xOffset); x++) {
                int image2Color = yColors2[x + xOffset];

                if (checkImage2WhitePixelsOnly && image2Color != 1)
                    continue;

                if (yColors1[x] == image2Color)
                    matches++;

                checked++;
            }
        }

        return new ComparisonResult(checked, matches);
    }

    public static boolean isFilled(BufferedImage image, int x, int y, int radius) {
        int filled = 0, empty = 0;

        for (int xOffset = Math.max(-radius, -x); xOffset < Math.min(radius, image.getWidth() - x); xOffset++) {
            final int currentX = x + xOffset;

            for (int yOffset = Math.max(-radius, -y); yOffset < Math.min(radius, image.getHeight() - y); yOffset++) {
                final int currentY = y + yOffset;

                if (image.getRGB(currentX, currentY) == Color.white.getRGB())
                    filled++;
                else
                    empty++;
            }
        }

        return filled > empty;
    }

}
