package dev.justix.gtavtools.tools.mission;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;

import static dev.justix.gtavtools.util.SystemUtil.*;

public class DrainageGrate extends Tool {

    private boolean cancel;

    public DrainageGrate(Logger logger) {
        super(logger, Category.MISSION, "Drainage Grate");

        this.cancel = false;
    }

    @Override
    public void execute() {
        logger.log(Level.INFO, "Removing grate bars...");

        sleep(500);
        mouseClick("LEFT", (2 * 7 + 2 * 9) * 6 * 185 + 1_000L);

        String[] keys = new String[] { "D", "S", "A", "W" };

        for (int line = 0; line < 4; line++) {
            boolean horizontal = (line % 2) == 0;

            for (int i = 0; i < (horizontal ? 7 : 9) * 6; i++) {
                if (this.cancel)
                    return;

                keyPress(keys[line], horizontal ? 21 : 23);
                sleep(185);
            }
        }

        logger.log(Level.INFO, "Grate bars removed successfully");
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
