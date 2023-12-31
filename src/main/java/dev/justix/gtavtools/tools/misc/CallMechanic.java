package dev.justix.gtavtools.tools.misc;

import dev.justix.gtavtools.config.ApplicationConfig;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;
import dev.justix.gtavtools.util.SystemUtil;

public class CallMechanic extends Tool {

    private boolean cancel;

    public CallMechanic(Logger logger) {
        super(logger, Category.MISC, "Call Mechanic");

        this.cancel = false;
    }

    @Override
    public void execute() {
        InterfaceNavigationUtil.openPhoneContacts();

        for (int i = 0; i < ApplicationConfig.CONFIG.getJsonObject("contactIndices").getInt("Mechanic"); i++) {
            if (this.cancel)
                return;

            SystemUtil.keyPress("DOWN", 15);
            SystemUtil.sleep(150);
        }

        SystemUtil.keyPress("ENTER", 15);
        SystemUtil.sleep(20);
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
