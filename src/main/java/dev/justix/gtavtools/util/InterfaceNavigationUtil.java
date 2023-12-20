package dev.justix.gtavtools.util;

import static dev.justix.gtavtools.util.SystemUtil.keyPress;
import static dev.justix.gtavtools.util.SystemUtil.sleep;

public class InterfaceNavigationUtil {

    public static void openPlayOnlineOptions(boolean isStoryMode) {
        keyPress("ESCAPE", 25);
        sleep(500);

        // Switch to 'Online' tab
        for (int i = 0; i < (isStoryMode ? 5 : 1); i++) {
            keyPress("RIGHT", 25L);
            sleep(75L);
        }

        sleep(750L);

        // Open tab
        keyPress("ENTER", 25L);
        sleep(750L);

        // 'Play GTA Online'
        for (int i = 0; i < (isStoryMode ? 1 : 4); i++) {
            keyPress("UP", 25L);
            sleep(75L);
        }

        sleep(250L);

        keyPress("ENTER", 25L);
        sleep(500L);
    }

}
