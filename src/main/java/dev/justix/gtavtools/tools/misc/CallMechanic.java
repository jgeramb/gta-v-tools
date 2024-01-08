package dev.justix.gtavtools.tools.misc;

import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;

public class CallMechanic extends Tool {

    private static final String NUMBER = "328-555-0153";

    public CallMechanic(Logger logger) {
        super(logger, Category.MISC, "Call Mechanic");
    }

    @Override
    public void execute() {
        InterfaceNavigationUtil.callNumber(NUMBER);
    }

}
