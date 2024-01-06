package dev.justix.gtavtools.tools.cayoperico;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;

import static dev.justix.gtavtools.util.ImageUtil.*;
import static dev.justix.gtavtools.util.SystemUtil.*;

public class SignalBoxHack extends Tool {

    private static final int FILL_RADIUS = 3;

    private final BufferedImage factor1, factor2, factor10;

    public SignalBoxHack(Logger logger) {
        super(logger, Category.CAYO_PERICO, "Signal Box Hack");

        this.relativeData.addRect("1920x1200", "result", 834, 132, 246, 90);
        this.relativeData.addRect("1920x1200", "result_number1", 0, 0, 45, 90);
        this.relativeData.addRect("1920x1200", "result_number2", 101, 0, 45, 90);
        this.relativeData.addRect("1920x1200", "result_number3", 201, 0, 45, 90);
        this.relativeData.addRect("1920x1200", "number1", 441, 297, 45, 90);
        this.relativeData.addRect("1920x1200", "number2", 441, 559, 45, 90);
        this.relativeData.addRect("1920x1200", "number3", 441, 819, 45, 90);
        this.relativeData.addRect("1920x1200", "factor1", 1390, 301, 78, 78);
        this.relativeData.addRect("1920x1200", "factor2", 1390, 562, 78, 78);
        this.relativeData.addRect("1920x1200", "factor3", 1390, 823, 78, 78);

        this.relativeData.addRect("1920x1080", "result", 847, 120, 220, 80);
        this.relativeData.addRect("1920x1080", "result_number1", 0, 0, 40, 80);
        this.relativeData.addRect("1920x1080", "result_number2", 90, 0, 40, 80);
        this.relativeData.addRect("1920x1080", "result_number3", 180, 0, 40, 80);
        this.relativeData.addRect("1920x1080", "number1", 492, 267, 40, 80);
        this.relativeData.addRect("1920x1080", "number2", 492, 503, 40, 80);
        this.relativeData.addRect("1920x1080", "number3", 492, 737, 40, 80);
        this.relativeData.addRect("1920x1080", "factor1", 1346, 271, 73, 73);
        this.relativeData.addRect("1920x1080", "factor2", 1346, 506, 73, 73);
        this.relativeData.addRect("1920x1080", "factor3", 1346, 741, 73, 73);

        BufferedImage factor1Image = null, factor2Image = null, factor10Image = null;

        try {
            factor1Image = comparableImage(fromResource("/mission-images/hack-factor-1.png"));
            factor2Image = comparableImage(fromResource("/mission-images/hack-factor-2.png"));
            factor10Image = comparableImage(fromResource("/mission-images/hack-factor-10.png"));
        } catch (IOException ignore) {
        }

        this.factor1 = factor1Image;
        this.factor2 = factor2Image;
        this.factor10 = factor10Image;
    }

    @Override
    public void execute() {
        BufferedImage resultImage = screenshot(this.relativeData.getRect("result"));
        int result = fromDigital(crop(resultImage, this.relativeData.getRect("result_number1"))) * 100
                + fromDigital(crop(resultImage, this.relativeData.getRect("result_number2"))) * 10
                + fromDigital(crop(resultImage, this.relativeData.getRect("result_number3"))),
                number1 = fromDigital(screenshot(this.relativeData.getRect("number1"))),
                number2 = fromDigital(screenshot(this.relativeData.getRect("number2"))),
                number3 = fromDigital(screenshot(this.relativeData.getRect("number3")));
        int[] factors = new int[] {
                getFactor(screenshot(this.relativeData.getRect("factor1"))),
                getFactor(screenshot(this.relativeData.getRect("factor2"))),
                getFactor(screenshot(this.relativeData.getRect("factor3")))
        };

        logger.log(Level.INFO, "Calculating...");

        if (DEBUG) {
            logger.log(Level.INFO, "Expected result: " + result);
            logger.log(Level.INFO, "Numbers: " + number1 + "; " + number2 + "; " + number3);
            logger.log(Level.INFO, "Factors: " + factors[0] + "; " + factors[1] + "; " + factors[2]);
        }

        for (int factor1Index = 0; factor1Index < factors.length; factor1Index++) {
            int factor1 = factors[factor1Index];

            for (int factor2Index = 0; factor2Index < factors.length; factor2Index++) {
                int factor2 = factors[factor2Index];

                // No duplicate factors
                if (factor1Index == factor2Index)
                    continue;

                for (int factor3Index = 0; factor3Index < factors.length; factor3Index++) {
                    int factor3 = factors[factor3Index];

                    // No duplicate factors
                    if (factor1Index == factor3Index)
                        continue;

                    if (factor2Index == factor3Index)
                        continue;

                    // Check if factors match result
                    if (((factor1 * number1) + (factor2 * number2) + (factor3 * number3)) == result) {
                        if (DEBUG) {
                            logger.log(
                                    Level.INFO,
                                    "Solution: " +
                                            "1 -> " + (factor1Index + 1) + "; " +
                                            "2 -> " + (factor2Index + 1) + "; " +
                                            "3 -> " + (factor3Index + 1)
                            );
                        } else {
                            int[] solution = new int[]{factor1Index, factor2Index, factor3Index};

                            for (int numberIndex = 0; numberIndex < solution.length; numberIndex++) {
                                keyPress("ENTER", 25);
                                sleep(500);

                                if (numberIndex == 0) {
                                    for (int i = 0; i < solution[0]; i++) {
                                        keyPress("DOWN", 25);
                                        sleep(250);
                                    }
                                } else if (numberIndex == 1) {
                                    if ((solution[1] == 2) && (solution[0] == 0)) {
                                        keyPress("DOWN", 25);
                                        sleep(250);
                                    } else if (solution[1] == 0) {
                                        keyPress("UP", 25);
                                        sleep(250);
                                    }
                                }

                                keyPress("ENTER", 25);
                                sleep(1750);
                            }
                        }
                        return;
                    }
                }
            }
        }

        logger.log(Level.SEVERE, "No solution found");
    }

    private int getFactor(BufferedImage source) {
        BufferedImage image = comparableImage(source);
        double factor1Match = getMaxMatchPercentage(this.factor1, image, 100f, 13, 0),
                factor2Match = getMaxMatchPercentage(this.factor2, image, 100f, 8, 19),
                factor10Match = getMaxMatchPercentage(this.factor10, image, 100f, 2, 2);

        if ((factor1Match > factor2Match) && (factor1Match > factor10Match))
            return 1;
        else if ((factor2Match > factor1Match) && (factor2Match > factor10Match))
            return 2;
        else if ((factor10Match > factor1Match) && (factor10Match > factor2Match))
            return 10;

        return 0;
    }

    private static int fromDigital(BufferedImage source) {
        final int width = source.getWidth(), height = source.getHeight();
        final BufferedImage image = comparableImage(source);

        boolean is1 = isFilled(image, width / 2, 3, FILL_RADIUS),
                is2 = isFilled(image, 3, height / 4, FILL_RADIUS),
                is3 = isFilled(image, width - 3, height / 4, FILL_RADIUS),
                is4 = isFilled(image, width / 2, height / 2 - 5, FILL_RADIUS),
                is5 = isFilled(image, 3, (int) (height * 0.75), FILL_RADIUS),
                is6 = isFilled(image, width - 3, (int) (height * 0.75), FILL_RADIUS);

        if (is1 && is2) {
            if (is5) {
                if (is3) {
                    if (is4)
                        return 8;
                    else
                        return 0;
                } else
                    return 6;
            } else if (is3)
                return 9;
            else
                return 5;
        } else if (is3) {
            if (is6) {
                if (is4) {
                    if (is1)
                        return 3;
                    else
                        return 4;
                } else if (is1)
                    return 7;
                else
                    return 1;
            } else
                return 2;
        }

        // invalid
        return -1;
    }

    private static BufferedImage comparableImage(BufferedImage source) {
        new RescaleOp(1.2f, 35, null).filter(source, source);

        return transform(source, true);
    }

}
