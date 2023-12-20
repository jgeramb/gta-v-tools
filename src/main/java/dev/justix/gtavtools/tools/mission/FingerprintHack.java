package dev.justix.gtavtools.tools.mission;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.blocks.BlockMatrix;
import dev.justix.gtavtools.util.blocks.BlockMatrixConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import static dev.justix.gtavtools.util.ImageUtil.*;
import static dev.justix.gtavtools.util.SystemUtil.*;

public class FingerprintHack extends Tool {

    private boolean cancel;

    public FingerprintHack(Logger logger) {
        super(logger, Category.MISSION, "Fingerprint Hack");

        this.cancel = false;
    }

    @Override
    public void execute() throws InterruptedException {
        long startMillis = System.currentTimeMillis();

        logger.log(Level.INFO, "Hacking fingerprint...");

        // capture full screen
        BufferedImage screen = screenshot(0, 0, 1920, 1200);

        // expected fingerprint
        BufferedImage expectedCapture = crop(screen, 1040, 384, 448, 680);
        new RescaleOp(12f, -24, null).filter(expectedCapture, expectedCapture);

        final BufferedImage expected = transform(expectedCapture, 0, 0, 410, 618, true);
        final int blockSize = BlockMatrixConverter.getBlockSize(getPixels(expected));
        final Rectangle bounds = BlockMatrixConverter.getLastBounds();

        // split into 8 parts
        final BlockMatrix[] expectedParts = new BlockMatrix[8];
        final CountDownLatch expectedPartsLatch = new CountDownLatch(8);

        for (int currentElement = 0; currentElement < 8; currentElement++) {
            int finalCurrentElement = currentElement;

            new Thread(() -> {
                BufferedImage partCapture = crop(expected, bounds.x, (int) (finalCurrentElement * 77.5) + (bounds.y - 9), bounds.width, 66);
                BlockMatrix part = BlockMatrixConverter.convertBufferedImage(partCapture, blockSize);

                debug(finalCurrentElement + "_expected", part.getPixels());

                expectedParts[finalCurrentElement] = part;
                expectedPartsLatch.countDown();
            }).start();
        }

        // current fingerprint parts
        BufferedImage currentPartsCapture = crop(screen, 366, 397, 410, 662);
        new RescaleOp(16f, -72, null).filter(currentPartsCapture, currentPartsCapture);

        final BufferedImage currentPartsImage = transform(currentPartsCapture, true);

        // split into 8 parts
        final BlockMatrix[] currentParts = new BlockMatrix[8];
        final CountDownLatch currentPartsLatch = new CountDownLatch(8);

        for (int currentElement = 0; currentElement < 8; currentElement++) {
            int finalCurrentElement = currentElement;

            new Thread(() -> {
                // crop fingerprint to scan to current part
                BufferedImage partCapture = crop(currentPartsImage, bounds.x, (int) (finalCurrentElement * 84.5), bounds.width, 66);
                BlockMatrix part = BlockMatrixConverter.convertBufferedImage(partCapture, blockSize);

                debug(String.valueOf(finalCurrentElement), part.getPixels());

                currentParts[finalCurrentElement] = part;
                currentPartsLatch.countDown();
            }).start();
        }

        expectedPartsLatch.await();
        currentPartsLatch.await();

        if (cancel)
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
                    double currentMatchPercentage = expectedParts[i].compare(currentPart);

                    if (currentMatchPercentage > highestMatchPercentage) {
                        matchingIndex = i;
                        highestMatchPercentage = currentMatchPercentage;
                    }
                }

                if (DEBUG) {
                    if (finalCurrentElement == matchingIndex)
                        System.out.printf(Locale.US, "Part %d is correct\n", finalCurrentElement);
                    else
                        System.out.printf(Locale.US, "Part %d is %d%% similar to part %d\n", finalCurrentElement, Math.round(highestMatchPercentage * 100), matchingIndex);
                }

                indices[finalCurrentElement] = Math.max(matchingIndex, 0);
                indicesLatch.countDown();
            }).start();
        }

        indicesLatch.await();

        if (cancel)
            return;

        logger.log(Level.INFO, "Computation completed in " + (System.currentTimeMillis() - startMillis) + "ms");

        if (!(DEBUG)) {
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
                        if (cancel)
                            return;

                        keyPress(right ? "D" : "A", 8);
                        sleep(12);
                    }
                }

                if (cancel)
                    return;

                keyPress("S", 8);
                sleep(12);
            }
        }
    }

    @Override
    public void forceStop() {
        cancel = true;
    }

}
