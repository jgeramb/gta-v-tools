package dev.justix.gtavtools.tools.combat;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.SystemUtil;

public class AutoShoot extends Tool {


    private static final int TARGET_COLOR = -4108465;

    private boolean enabled;

    public AutoShoot(Logger logger) {
        super(logger, Category.COMBAT, "Auto Shoot");

        this.enabled = false;
    }

    @Override
    public void execute() {
        this.enabled = !this.enabled;
        logger.log(Level.INFO, "Auto shoot: " + (this.enabled ? "enabled" : "disabled"));

        if(this.enabled) {
            final int x = screenWidth() / 2, y = screenHeight() / 2;

            final Object shootLock = new Object();

            new Thread(() -> {
                while (this.enabled) {
                    if (SystemUtil.getScreenPixelColor(x, y).getRGB() == TARGET_COLOR) {
                        synchronized (shootLock) {
                            shootLock.notify();
                        }
                    }
                }
            }).start();

            while (this.enabled) {
                synchronized (shootLock) {
                    try {
                        shootLock.wait();

                        SystemUtil.mouseClick("LEFT", 10);
                        SystemUtil.sleep(15);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }
    }

    @Override
    public void forceStop() {
        this.enabled = false;
    }

}
