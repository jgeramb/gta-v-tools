package dev.justix.gtavtools.tools.misc;

import dev.justix.gtavtools.config.ApplicationConfig;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.SystemUtil;

public class RepairAllVehicles extends Tool {

    private boolean cancel;

    public RepairAllVehicles(Logger logger) {
        super(logger, Category.MISC, "Repair All Vehicles");

        this.cancel = false;
    }

    @Override
    public void execute() {
        SystemUtil.keyPress("UP", 15);
        SystemUtil.sleep(600);

        SystemUtil.keyPress("UP", 15);
        SystemUtil.sleep(80);

        SystemUtil.keyPress("RIGHT", 15);
        SystemUtil.sleep(80);

        SystemUtil.keyPress("ENTER", 15);
        SystemUtil.sleep(500);

        for (int i = 0; i < ApplicationConfig.CONFIG.getJsonObject("contactIndices").getInt("Mors Mutual Insurance"); i++) {
            if (this.cancel)
                return;

            SystemUtil.keyPress("DOWN", 15);
            SystemUtil.sleep(150);
        }

        SystemUtil.keyPress("ENTER", 15);
        SystemUtil.sleep(6000);

        SystemUtil.keyPress("UP", 15);
        SystemUtil.sleep(20);

        SystemUtil.keyPress("ENTER", 15);
        SystemUtil.sleep(20);
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
