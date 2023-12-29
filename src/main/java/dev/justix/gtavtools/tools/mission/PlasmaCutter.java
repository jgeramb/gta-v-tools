package dev.justix.gtavtools.tools.mission;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.SystemUtil;

public class PlasmaCutter extends Tool {

    private boolean cancel;

    public PlasmaCutter(Logger logger) {
        super(logger, Category.MISSION, "Plasma Cutter");

        this.cancel = false;
    }

    @Override
    public void execute() {
        logger.log(Level.INFO, "Cutting glass...");

        for (int i = 0; i < 5; i++) {
            if (this.cancel)
                return;

            SystemUtil.keyPress("PAGE_UP", 1975);
            SystemUtil.sleep(3150);
        }

        logger.log(Level.INFO, "Glass cut successfully");
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
