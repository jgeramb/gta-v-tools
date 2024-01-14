package dev.justix.gtavtools.util.blocks;

import dev.justix.gtavtools.util.images.ImageUtil;
import lombok.Getter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class BlockMatrixConverter {

    @Getter
    private static Rectangle lastBounds;

    public static BlockMatrix convertBufferedImage(BufferedImage image, int blockSize) {
        return new BlockMatrix(
                (int) Math.ceil((double) image.getWidth() / blockSize),
                (int) Math.ceil((double) image.getHeight() / blockSize),
                getBlocks(ImageUtil.getPixels(image), blockSize)
        );
    }

    public static BlockMatrix convertPixels(int[][] pixels, int blockSize) {
        return new BlockMatrix(
                (int) Math.ceil((double) pixels[0].length / blockSize),
                (int) Math.ceil((double) pixels.length / blockSize),
                getBlocks(pixels, blockSize)
        );
    }

    public static int getBlockSize(int[][] pixels) {
        float blockSizeSum = 0;
        int blockCount = 0;

        int minX = pixels[0].length, minY = pixels.length, maxX = 0, maxY = 0;

        int y = 0, x = 0;
        int[] row = pixels[0];

        while (x < row.length) {
            if (row[x] == 1) {
                int blockWidth = 1;

                for (int x2 = x + 1; x2 < row.length; x2++) {
                    if (row[x2] == 0) break;

                    blockWidth++;
                }

                int blockHeight = 1;

                for (int y2 = y + 1; y2 < pixels.length; y2++) {
                    if (pixels[y2][x] == 0) break;

                    blockHeight++;
                }

                float ratio = (float) (blockWidth + blockHeight) / 2;

                if (blockCount == 0 || (ratio < (blockSizeSum / blockCount) * 2)) {
                    blockSizeSum += ratio;
                    blockCount++;
                }

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x + blockWidth);
                maxY = Math.max(maxY, y + blockHeight);
            }

            // update coordinates
            if (++x >= row.length) {
                x = 0;
                y++;

                if (y >= pixels.length) break;

                row = pixels[y];
            }
        }

        lastBounds = new Rectangle(minX, minY, maxX - minX, maxY - minY);

        return Math.round(blockSizeSum / blockCount);
    }

    private static List<Block> getBlocks(int[][] pixels, int blockSize) {
        final List<Block> blocks = new ArrayList<>();

        int y = 0, x = 0;

        while (x < pixels[0].length) {
            int filledPixels = 0, emptyPixels = 0;

            for (int y2 = y; y2 < Math.min(y + blockSize, pixels.length); y2++) {
                final int[] row = pixels[y2];

                for (int x2 = x; x2 < Math.min(x + blockSize, row.length); x2++) {
                    if (row[x2] == 1) filledPixels++;
                    else emptyPixels++;
                }
            }

            blocks.add(new Block(x / blockSize, y / blockSize, blockSize, filledPixels > emptyPixels * 2f));

            // update coordinates
            x += blockSize;

            if (x >= pixels[0].length) {
                x = 0;
                y += blockSize;

                if (y >= pixels.length) break;
            }
        }

        return blocks;
    }

}
