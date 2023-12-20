package dev.justix.gtavtools.tools.mission;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.SystemUtil;

public class CollectLoot extends Tool {

    private boolean collecting;
    private Thread thread;

    public CollectLoot(Logger logger) {
        super(logger, Category.MISSION, "Collect Loot");

        this.collecting = false;
        this.thread = null;
    }

    @Override
    public void execute() {
        if (collecting)
            logger.log(Level.WARNING, "Loot collection interrupted");
        else {
            logger.log(Level.INFO, "Collecting loot...");

            thread = new Thread(() -> {
                Thread.currentThread().setName("Loot collection");

                for (int i = 0; i < 34; i++) {
                    if (!(collecting)) return;

                    SystemUtil.mouseClick("LEFT", 25);
                    SystemUtil.sleep(250);
                }

                logger.log(Level.INFO, "Collected loot successfully");

                collecting = false;
            });
            thread.start();
        }

        collecting = !(collecting);
    }

    @Override
    public void forceStop() {
        collecting = false;

        thread.interrupt();
    }

}
