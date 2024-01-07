package dev.justix.gtavtools.tools.cayoperico;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.SystemUtil;

public class CollectLoot extends Tool {

    private boolean collecting;

    public CollectLoot(Logger logger) {
        super(logger, Category.CAYO_PERICO, "Collect Loot");

        this.collecting = false;
    }

    @Override
    public void execute() {
        this.collecting = !this.collecting;

        if (!this.collecting) {
            logger.log(Level.WARNING, "Loot collection interrupted");
            return;
        }

        logger.log(Level.INFO, "Collecting loot...");

        new Thread(() -> {
            Thread.currentThread().setName("Loot collection");

            for (int i = 0; i < 34; i++) {
                if (!this.collecting) return;

                SystemUtil.mouseClick("LEFT", 25);
                SystemUtil.sleep(250);
            }

            logger.log(Level.INFO, "Collected loot successfully");

            this.collecting = false;
        }).start();
    }

    @Override
    public void forceStop() {
        this.collecting = false;
    }

}
