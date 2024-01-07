package dev.justix.gtavtools.util.blocks;

import dev.justix.gtavtools.util.ImageUtil;
import lombok.Data;

import java.util.List;

@Data
public class BlockMatrix {

    private final int width, height;
    private final int[][] pixels;

    public BlockMatrix(int width, int height, List<Block> blocks) {
        this.width = width;
        this.height = height;
        this.pixels = new int[height][width];

        blocks.forEach(block -> this.pixels[block.y()][block.x()] = block.filled() ? 1 : 0);
    }

    public double compare(BlockMatrix other) {
        return ImageUtil.getMaxMatchPercentage(this.pixels, other.getPixels(), 100f, 1, 1);
    }

    public double compare(BlockMatrix other, float maxRequiredMaxPercentage, int minXOffset, int maxXOffset, int minYOffset, int maxYOffset, boolean checkImage2WhitePixelsOnly) {
        return ImageUtil.getMaxMatchPercentage(
                this.pixels, other.getPixels(),
                maxRequiredMaxPercentage,
                minXOffset, maxXOffset,
                minYOffset, maxYOffset,
                checkImage2WhitePixelsOnly
        );
    }

}
