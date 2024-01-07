package dev.justix.gtavtools.util;

import dev.justix.gtavtools.tools.RelativeToolData;

import static dev.justix.gtavtools.util.SystemUtil.*;

public class InterfaceNavigationUtil {

    private static final RelativeToolData RELATIVE_DATA = new RelativeToolData();

    static {
        RELATIVE_DATA.addRect("1920x1200", "interaction_menu_ceo", 35, 175, 190, 24);
        RELATIVE_DATA.addRect("1920x1080", "interaction_menu_ceo", 35, 160, 175, 22);
    }

    public static void openPlayOnlineOptions(boolean isStoryMode) {
        keyPress("ESCAPE", 25);
        sleep(500);

        // Switch to 'Online' tab
        for (int i = 0; i < (isStoryMode ? 6 : 1); i++) {
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

    public static void openPhoneContacts() {
        SystemUtil.keyPress("UP", 15);
        SystemUtil.sleep(600);

        SystemUtil.keyPress("UP", 15);
        SystemUtil.sleep(80);

        SystemUtil.keyPress("RIGHT", 15);
        SystemUtil.sleep(80);

        SystemUtil.keyPress("ENTER", 15);
        SystemUtil.sleep(500);
    }

    public static void openPlaningScreen() {
        // Register as CEO
        keyPress("M", 25);
        sleep(250);

        if (OCRUtil.ocr(screenshot(RELATIVE_DATA.getRect("interaction_menu_ceo")), true).equals("SecuroServ-CEO")) {
            keyPress("ESCAPE", 15);
            sleep(20);
        } else {
            for (int i = 0; i < 2; i++) {
                keyPress("DOWN", 15);
                sleep(20);
            }

            for (int i = 0; i < 3; i++) {
                keyPress("ENTER", 15);
                sleep(20);
            }

            sleep(750);
        }

        // Open planing screen
        keyPress("E", 15);
        sleep(6000);
    }

    public static void startMission() {
        // Confirm mission settings
        keyPress("UP", 10);
        sleep(50);

        keyPress("ENTER", 10);
        sleep(450);

        // Start mission
        keyPress("UP", 10);
        sleep(50);

        keyPress("ENTER", 10);
        sleep(750);

        keyPress("ENTER", 15);
        sleep(14_500L);
    }

}
