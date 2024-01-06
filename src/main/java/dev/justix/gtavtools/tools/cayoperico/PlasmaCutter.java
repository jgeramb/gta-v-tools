package dev.justix.gtavtools.tools.cayoperico;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;

import static dev.justix.gtavtools.util.SystemUtil.keyPress;
import static dev.justix.gtavtools.util.SystemUtil.sleep;

public class PlasmaCutter extends Tool {

    private boolean cancel;

    public PlasmaCutter(Logger logger) {
        super(logger, Category.CAYO_PERICO, "Plasma Cutter");

        this.cancel = false;
    }

    @Override
    public void execute() {
        logger.log(Level.INFO, "Cutting glass...");

        for (int i = 0; i < 5; i++) {
            if (this.cancel) return;

            keyPress("PAGE_UP", 1975);
            sleep(3150);
        }

        logger.log(Level.INFO, "Glass cut successfully");
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
