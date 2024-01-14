package dev.justix.gtavtools.tools.cayoperico;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.blocks.BlockMatrix;
import dev.justix.gtavtools.util.blocks.BlockMatrixConverter;
import dev.justix.gtavtools.util.ocr.OCRUtil;
import dev.justix.gtavtools.util.ocr.Symbols;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import static dev.justix.gtavtools.util.SystemUtil.*;
import static dev.justix.gtavtools.util.images.ImageUtil.*;

public class FingerprintHack extends Tool {

    private boolean cancel;

    public FingerprintHack(Logger logger) {
        super(logger, Category.CAYO_PERICO, "Fingerprint Hack");

        relativeData.addRect("1920x1200", "components_text", 512, 334, 26, 18);
        relativeData.add("1920x1200", "part_height", 66);
        relativeData.addRect("1920x1200", "expected", 1040, 384, 448, 680);
        relativeData.addRect("1920x1200", "expected_resized", 0, 0, 410, 618);
        relativeData.add("1920x1200", "expected_y_offset", 9);
        relativeData.addRect("1920x1200", "current", 366, 397, 410, 662);
        relativeData.add("1920x1200", "current_y_step", 84.5d);

        relativeData.add("1920x1080", "part_height", 61);
        relativeData.addRect("1920x1080", "expected", 1032, 348, 406, 602);
        relativeData.addRect("1920x1080", "expected_resized", 0, 0, 366, 550);
        relativeData.add("1920x1080", "expected_y_offset", 3);
        relativeData.addRect("1920x1080", "current", 428, 358, 366, 592);
        relativeData.add("1920x1080", "current_y_step", 75d);

        this.cancel = false;
    }

    @Override
    public void execute() throws InterruptedException {
        BufferedImage componentsTextCapture;

        do {
            long startMillis = System.currentTimeMillis();

            logger.log(Level.INFO, "Hacking fingerprint...");

            // capture full screen
            BufferedImage screen = screenshot(0, 0, screenWidth(), screenHeight());

            // expected fingerprint
            BufferedImage expectedCapture = crop(screen, relativeData.getRect("expected"));
            new RescaleOp(12f, -24, null).filter(expectedCapture, expectedCapture);

            final BufferedImage expected = transform(expectedCapture, relativeData.getRect("expected_resized"), true);
            final double expectedYStep = expected.getHeight() / 8d;
            final int blockSize = BlockMatrixConverter.getBlockSize(getPixels(expected));
            final Rectangle bounds = BlockMatrixConverter.getLastBounds();

            // split into 8 parts
            final BlockMatrix[] expectedParts = new BlockMatrix[8];
            final CountDownLatch expectedPartsLatch = new CountDownLatch(8);

            for (int currentElement = 0; currentElement < 8; currentElement++) {
                int finalCurrentElement = currentElement;

                new Thread(() -> {
                    final int currentY = (int) (finalCurrentElement * expectedYStep) + (bounds.y - relativeData.getNumber("expected_y_offset"));
                    BufferedImage partCapture = crop(expected, bounds.x, currentY, bounds.width, relativeData.getNumber("part_height"));
                    BlockMatrix part = BlockMatrixConverter.convertBufferedImage(partCapture, blockSize);

                    debug(finalCurrentElement + "_expected", part.getPixels());

                    expectedParts[finalCurrentElement] = part;
                    expectedPartsLatch.countDown();
                }).start();
            }

            // current fingerprint parts
            BufferedImage currentPartsCapture = crop(screen, relativeData.getRect("current"));
            new RescaleOp(16f, -72, null).filter(currentPartsCapture, currentPartsCapture);

            final BufferedImage currentPartsImage = transform(currentPartsCapture, true);

            // split into 8 parts
            final BlockMatrix[] currentParts = new BlockMatrix[8];
            final CountDownLatch currentPartsLatch = new CountDownLatch(8);

            for (int currentElement = 0; currentElement < 8; currentElement++) {
                int finalCurrentElement = currentElement;

                new Thread(() -> {
                    // crop fingerprint to scan to current part
                    final int currentY = (int) (finalCurrentElement * relativeData.getDecimal("current_y_step"));

                    BufferedImage partCapture = crop(currentPartsImage, bounds.x, currentY, bounds.width, relativeData.getNumber("part_height"));
                    BlockMatrix part = BlockMatrixConverter.convertBufferedImage(partCapture, blockSize);

                    debug(String.valueOf(finalCurrentElement), part.getPixels());

                    currentParts[finalCurrentElement] = part;
                    currentPartsLatch.countDown();
                }).start();
            }

            expectedPartsLatch.await();
            currentPartsLatch.await();

            if (this.cancel)
                return;

            // determine correct indices for each part of the fingerprint to scan
            final int[] indices = new int[8];
            final CountDownLatch indicesLatch = new CountDownLatch(8);

            for (int currentElement = 0; currentElement < 8; currentElement++) {
                int finalCurrentElement = currentElement;
                BlockMatrix currentPart = currentParts[currentElement];

                new Thread(() -> {
                    int matchingIndex = -1;
                    double highestMatchPercentage = 0d;

                    for (int i = 0; i < expectedParts.length; i++) {
                        double currentMatchPercentage = currentPart.compare(expectedParts[i]);

                        if (currentMatchPercentage > highestMatchPercentage) {
                            matchingIndex = i;
                            highestMatchPercentage = currentMatchPercentage;
                        }
                    }

                    if (DEBUG) {
                        if (finalCurrentElement == matchingIndex)
                            logger.log(Level.INFO, String.format(Locale.US, "Part %d is correct\n", finalCurrentElement));
                        else
                            logger.log(Level.INFO, String.format(Locale.US, "Part %d is %d%% similar to part %d\n", finalCurrentElement, Math.round(highestMatchPercentage * 100), matchingIndex));
                    }

                    indices[finalCurrentElement] = Math.max(matchingIndex, 0);
                    indicesLatch.countDown();
                }).start();
            }

            indicesLatch.await();

            if (this.cancel)
                return;

            logger.log(Level.INFO, "Computation completed in " + (System.currentTimeMillis() - startMillis) + "ms");

            if (!DEBUG) {
                // change elements based on the difference between the correct index and the one of the part currently shown
                for (int currentIndex = 0; currentIndex < indices.length; currentIndex++) {
                    int indexDiff = currentIndex - indices[currentIndex];

                    if (indexDiff != 0) {
                        boolean right = true;

                        if (indexDiff < 0) {
                            indexDiff = Math.abs(indexDiff);
                            right = false;
                        }

                        if (indexDiff > 4) {
                            indexDiff = 8 - indexDiff;
                            right = !(right);
                        }

                        for (int i = 0; i < indexDiff; i++) {
                            if (this.cancel)
                                return;

                            keyPress(right ? "D" : "A", 10);
                            sleep(14);
                        }
                    }

                    if (this.cancel)
                        return;

                    keyPress("S", 10);
                    sleep(14);
                }
            }

            sleep(3_950);

            BufferedImage textCapture = screenshot(relativeData.getRect("components_text"));
            new RescaleOp(2.5f, -8, null).filter(textCapture, textCapture);

            componentsTextCapture = transform(textCapture, true);
        } while (OCRUtil.ocr(componentsTextCapture, Symbols.UPPERCASE_LETTERS).equalsIgnoreCase("CO"));
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
