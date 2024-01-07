package dev.justix.gtavtools.tools.casino;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;

import static dev.justix.gtavtools.util.SystemUtil.mouseClick;
import static dev.justix.gtavtools.util.SystemUtil.sleep;

public class Drill extends Tool {

    private boolean cancel;

    public Drill(Logger logger) {
        super(logger, Category.CASINO, "Drill");

        this.cancel = false;
    }

    @Override
    public void execute() {
        sleep(250);

        logger.log(Level.INFO, "Drilling through vault door...");

        for (int i = 0; i < 16; i++) {
            if (this.cancel) return;

            mouseClick("LEFT", 550);
            sleep(1400);
        }

        logger.log(Level.INFO, "Drilled through vault door successfully");
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
