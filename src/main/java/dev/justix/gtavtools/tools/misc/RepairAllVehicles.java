package dev.justix.gtavtools.tools.misc;

import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;

import static dev.justix.gtavtools.util.SystemUtil.keyPress;
import static dev.justix.gtavtools.util.SystemUtil.sleep;

public class RepairAllVehicles extends Tool {

    private static final String NUMBER = "611-555-0149";

    public RepairAllVehicles(Logger logger) {
        super(logger, Category.MISC, "Repair All Vehicles");
    }

    @Override
    public void execute() {
        InterfaceNavigationUtil.callNumber(NUMBER);

        sleep(6000);

        keyPress("UP", 10);
        sleep(25);

        keyPress("ENTER", 10);
        sleep(25);
    }

}
