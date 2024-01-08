package dev.justix.gtavtools.tools.misc;

import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;

public class CallPegasus extends Tool {

    private static final String NUMBER = "328-555-0122";

    public CallPegasus(Logger logger) {
        super(logger, Category.MISC, "Call Pegasus");
    }

    @Override
    public void execute() {
        InterfaceNavigationUtil.callNumber(NUMBER);
    }

}
