package dev.justix.gtavtools.tools.casino;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.OCRUtil;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.HashMap;
import java.util.Map;

import static dev.justix.gtavtools.util.ImageUtil.isFilled;
import static dev.justix.gtavtools.util.ImageUtil.transform;
import static dev.justix.gtavtools.util.SystemUtil.*;

public class KeypadHack extends Tool {

    private static final int FILL_RADIUS = 10;

    private boolean cancel;

    public KeypadHack(Logger logger) {
        super(logger, Category.CASINO, "Keypad Hack");

        this.relativeData.addRect("1920x1200", "repeater_text", 740, 244, 25, 18);
        this.relativeData.addRect("1920x1200", "grid", 398, 332, 696, 588);

        this.cancel = false;
    }

    @Override
    public void execute() {
        this.logger.log(Level.INFO, "Hacking Keypad...");

        int cursorY = 0;

        BufferedImage repeaterText;

        do {
            HashMap<Integer, Map<Integer, Integer>> cellCounts = new HashMap<>();
            long startMillis = System.currentTimeMillis();

            while(!this.cancel && System.currentTimeMillis() - startMillis < 3_650) {
                final BufferedImage grid = transform(screenshot(this.relativeData.getRect("grid")), true);
                int cellWidth = grid.getWidth() / 6;
                int cellHeight = grid.getHeight() / 5;
                int cellDiameter = (cellWidth + cellHeight) / 2;

                for(int x = 0; x < 6; x++) {
                    for (int y = 0; y < 5; y++) {
                        int currentX = x * cellWidth + cellDiameter / 2;
                        int currentY = y * cellHeight + cellDiameter / 2;

                        if(isFilled(grid, currentX, currentY, FILL_RADIUS)) {
                            cellCounts.putIfAbsent(x, new HashMap<>());

                            Map<Integer, Integer> yCounts = cellCounts.get(x);
                            yCounts.put(y, yCounts.getOrDefault(y, 0) + 1);
                            break;
                        }
                    }
                }

                sleep(100);
            }

            sleep(500);

            HashMap<Integer, Integer> cells = new HashMap<>();

            cellCounts.forEach((key, value) -> cells.put(
                    key,
                    value.entrySet()
                            .stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElseThrow(() -> new NullPointerException(String.format("No value found for x=%d", key)))
            ));

            for (int x = 0; x < 6; x++) {
                final int expectedY = cells.get(x);

                if(cursorY > expectedY) {
                    for (int y = cursorY; y > expectedY; y--) {
                        keyPress("UP", 8);
                        sleep(10);
                    }
                } else if(cursorY < expectedY) {
                    for (int y = cursorY; y < expectedY; y++) {
                        keyPress("DOWN", 8);
                        sleep(10);
                    }
                }

                cursorY = expectedY;

                keyPress("ENTER", 8);
                sleep(2_150);
            }

            sleep(2_250);

            BufferedImage textCapture = screenshot(this.relativeData.getRect("repeater_text"));
            new RescaleOp(0.85f, 8, null).filter(textCapture, textCapture);

            repeaterText = transform(textCapture, true);
        } while (OCRUtil.ocr(repeaterText, true).equals("RE"));

        this.logger.log(Level.INFO, "Keypad hacked successfully");
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
