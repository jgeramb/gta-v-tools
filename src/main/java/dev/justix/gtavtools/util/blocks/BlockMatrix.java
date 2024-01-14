package dev.justix.gtavtools.util.blocks;

import dev.justix.gtavtools.util.images.ImageUtil;
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
        return ImageUtil.getBestMatchPercentage(this.pixels, other.getPixels(), 1, 1);
    }

    public double compare(BlockMatrix other, int minXOffset, int maxXOffset, int minYOffset, int maxYOffset, boolean checkImage2WhitePixelsOnly) {
        return ImageUtil.getBestMatchPercentage(
                this.pixels, other.getPixels(),
                minXOffset, maxXOffset,
                minYOffset, maxYOffset,
                checkImage2WhitePixelsOnly
        );
    }

}
