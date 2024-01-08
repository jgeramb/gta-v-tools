package dev.justix.gtavtools.tools.misc;

import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;

public class CallMerryweather extends Tool {

    private static final String NUMBER = "273-555-0120";

    public CallMerryweather(Logger logger) {
        super(logger, Category.MISC, "Call Merryweather");
    }

    @Override
    public void execute() {
        InterfaceNavigationUtil.callNumber(NUMBER);
    }

}
