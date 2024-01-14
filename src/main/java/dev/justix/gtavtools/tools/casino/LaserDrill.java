package dev.justix.gtavtools.tools.casino;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;

import static dev.justix.gtavtools.util.SystemUtil.mouseClick;
import static dev.justix.gtavtools.util.SystemUtil.sleep;

public class LaserDrill extends Tool {

    private boolean cancel;

    public LaserDrill(Logger logger) {
        super(logger, Category.CASINO, "Laser Drill");

        this.cancel = false;
    }

    @Override
    public void execute() {
        // Focus game window
        mouseClick("LEFT", 25);
        sleep(500);

        logger.log(Level.INFO, "Drilling through vault door...");

        mouseClick("LEFT", 1_500);
        sleep(1_850);

        for (int i = 0; i < 17; i++) {
            if (this.cancel) return;

            mouseClick("LEFT", 440);
            sleep(1_220);
        }

        logger.log(Level.INFO, "Drilled through vault door successfully");
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
