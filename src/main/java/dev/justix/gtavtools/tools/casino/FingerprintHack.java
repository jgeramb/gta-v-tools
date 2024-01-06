package dev.justix.gtavtools.tools.casino;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.OCRUtil;
import dev.justix.gtavtools.util.blocks.BlockMatrix;
import dev.justix.gtavtools.util.blocks.BlockMatrixConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static dev.justix.gtavtools.util.ImageUtil.*;
import static dev.justix.gtavtools.util.SystemUtil.*;

public class FingerprintHack extends Tool {

    private boolean cancel;

    public FingerprintHack(Logger logger) {
        super(logger, Category.CASINO, "Fingerprint Hack");

        this.relativeData.addRect("1920x1200", "components_text", 502, 244, 26, 18);
        this.relativeData.addRect("1920x1200", "expected", 986, 174, 361, 560);
        this.relativeData.addRect("1920x1200", "expected_resized", 0, 0, 290, 430);
        this.relativeData.addPoint("1920x1200", "part_start_coordinates", 428, 308);
        this.relativeData.add("1920x1200", "part_size", 118);
        this.relativeData.add("1920x1200", "part_step", 160);

        this.cancel = false;
    }

    @Override
    public void execute() {
        BufferedImage componentsTextCapture;

        do {
            long startMillis = System.currentTimeMillis();

            BufferedImage expectedCapture = screenshot(this.relativeData.getRect("expected"));
            new RescaleOp(2f, -32, null).filter(expectedCapture, expectedCapture);

            final BufferedImage expectedResized = transform(expectedCapture, this.relativeData.getRect("expected_resized"), false);
            final int[][] expectedPixels = getPixels(expectedResized, 50, 120, 50, 120, 50, 120, false);
            final int blockSize = BlockMatrixConverter.getBlockSize(expectedPixels);
            final BlockMatrix expected = BlockMatrixConverter.convertPixels(expectedPixels, blockSize);

            debug("expected", expected.getPixels());

            final Point start = this.relativeData.getPoint("part_start_coordinates");
            final int size = this.relativeData.getNumber("part_size"),
                    step = this.relativeData.getNumber("part_step"),
                    comparisonPadding = 4;

            final BufferedImage partsCapture = screenshot(start.x, start.y, step + size, step * 3 + size);
            new RescaleOp(16f, -72, null).filter(partsCapture, partsCapture);

            final BufferedImage partsImage = transform(partsCapture, true);
            final Map<Integer, Double> indexMatches = new HashMap<>();
            final CountDownLatch indicesLatch = new CountDownLatch(8);

            for (int index = 0; index < 8; index++) {
                int finalIndex = index;

                new Thread(() -> {
                    final BufferedImage partCapture = crop(partsImage, (finalIndex % 2) * step, (finalIndex / 2) * step, size, size);
                    final BlockMatrix part = BlockMatrixConverter.convertBufferedImage(partCapture, blockSize);

                    debug("part_" + finalIndex, part.getPixels());

                    final double matchPercentage = part.compare(
                            expected,
                            87.5f,
                            -comparisonPadding,
                            expected.getWidth() - size / blockSize + comparisonPadding,
                            -comparisonPadding,
                            expected.getHeight() - size / blockSize + comparisonPadding,
                            true
                    );

                    indexMatches.put(finalIndex, matchPercentage);

                    if(DEBUG)
                        logger.log(Level.INFO, String.format(Locale.US, "Part %d matches %.2f%%", finalIndex, matchPercentage * 100));

                    indicesLatch.countDown();
                }).start();
            }

            try {
                indicesLatch.await();
            } catch (InterruptedException ignore) {
            }

            if (this.cancel)
                return;

            logger.log(Level.INFO, "Computation completed in " + (System.currentTimeMillis() - startMillis) + "ms");

            if(!DEBUG) {
                final List<Integer> bestMatchingIndices = indexMatches
                        .entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(entry -> (int) ((1 - entry.getValue()) * 100)))
                        .limit(4)
                        .map(Map.Entry::getKey)
                        .toList();

                for (int index = 0; index < 8; index++) {
                    if (bestMatchingIndices.contains(index)) {
                        keyPress("ENTER", 10);
                        sleep(14);
                    }

                    keyPress("RIGHT", 10);
                    sleep(14);
                }

                keyPress("TAB", 10);
                sleep(15);
            }

            sleep(4_350);

            BufferedImage textCapture = screenshot(this.relativeData.getRect("components_text"));
            new RescaleOp(0.85f, 8, null).filter(textCapture, textCapture);

            componentsTextCapture = transform(textCapture, true);
        } while (OCRUtil.ocr(componentsTextCapture, true).equals("CO"));
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
