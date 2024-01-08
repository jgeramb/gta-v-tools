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

        relativeData.addRect("1920x1200", "components_text", 502, 244, 26, 18);
        relativeData.addRect("1920x1200", "expected", 986, 174, 361, 560);
        relativeData.addRect("1920x1200", "expected_resized", 0, 0, 290, 430);
        relativeData.addPoint("1920x1200", "part_start_coordinates", 428, 308);
        relativeData.add("1920x1200", "part_size", 118);
        relativeData.add("1920x1200", "part_step", 160);

        relativeData.addRect("1920x1080", "components_text", 546, 220, 26, 18);
        relativeData.addRect("1920x1080", "expected", 960, 157, 410, 516);
        relativeData.addRect("1920x1080", "expected_resized", 0, 0, 328, 406);
        relativeData.addPoint("1920x1080", "part_start_coordinates", 480, 277);
        relativeData.add("1920x1080", "part_size", 106);
        relativeData.add("1920x1080", "part_step", 144);

        this.cancel = false;
    }

    @Override
    public void execute() {
        BufferedImage componentsTextCapture;

        do {
            long startMillis = System.currentTimeMillis();

            BufferedImage expectedCapture = screenshot(relativeData.getRect("expected"));
            new RescaleOp(2f, -32, null).filter(expectedCapture, expectedCapture);

            final BufferedImage expectedResized = transform(expectedCapture, relativeData.getRect("expected_resized"), false);
            final int[][] expectedPixels = getPixels(expectedResized, 20, 180, 20, 180, 20, 180, false);
            final int blockSize = BlockMatrixConverter.getBlockSize(expectedPixels);
            final BlockMatrix expected = BlockMatrixConverter.convertPixels(expectedPixels, blockSize);

            debug("expected", expected.getPixels());

            final Point start = relativeData.getPoint("part_start_coordinates");
            final int size = relativeData.getNumber("part_size"),
                    step = relativeData.getNumber("part_step"),
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

            sleep(4_500);

            BufferedImage textCapture = screenshot(relativeData.getRect("components_text"));
            new RescaleOp(0.85f, 8, null).filter(textCapture, textCapture);

            componentsTextCapture = transform(textCapture, true);
        } while (OCRUtil.ocr(componentsTextCapture, true).equals("CO"));
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
