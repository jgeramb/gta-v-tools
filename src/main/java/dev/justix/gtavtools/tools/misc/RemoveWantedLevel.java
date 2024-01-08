package dev.justix.gtavtools.tools.misc;

import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;

import static dev.justix.gtavtools.util.SystemUtil.keyPress;
import static dev.justix.gtavtools.util.SystemUtil.sleep;

public class RemoveWantedLevel extends Tool {

    private static final String NUMBER = "346-555-0102";

    public RemoveWantedLevel(Logger logger) {
        super(logger, Category.MISC, "Remove Wanted Level");
    }

    @Override
    public void execute() {
        InterfaceNavigationUtil.callNumber(NUMBER);

        sleep(6000);

        keyPress("DOWN", 10);
        sleep(25);

        keyPress("ENTER", 10);
        sleep(25);
    }

}
