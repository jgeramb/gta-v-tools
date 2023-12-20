package dev.justix.gtavtools.tools.combat;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.SystemUtil;

public class AutoShoot extends Tool {

    private boolean enabled;

    public AutoShoot(Logger logger) {
        super(logger, Category.COMBAT, "Auto Shoot");

        this.enabled = false;
    }

    @Override
    public void execute() {
        if (enabled)
            logger.log(Level.INFO, "Auto shoot: disabled");
        else {
            int target = -4108465;

            new Thread(() -> {
                while (enabled) {
                    if (SystemUtil.getScreenPixelColor(screenWidth() / 2, screenHeight() / 2).getRGB() == target) {
                        SystemUtil.mouseClick("LEFT", 15);
                        SystemUtil.sleep(15);
                    }

                    SystemUtil.sleep(8);
                }
            }).start();

            logger.log(Level.INFO, "Auto shoot: enabled");
        }

        enabled = !(enabled);
    }

    @Override
    public void forceStop() {
        enabled = false;
    }

}
