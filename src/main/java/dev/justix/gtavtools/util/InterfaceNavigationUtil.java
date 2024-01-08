package dev.justix.gtavtools.util;

import dev.justix.gtavtools.tools.RelativeToolData;

import java.awt.*;
import java.util.Map;

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

    private static final Map<Byte, Point> NUMBER_POSITIONS = Map.of(
            (byte) 1, new Point(0, 0),
            (byte) 2, new Point(1, 0),
            (byte) 3, new Point(2, 0),
            (byte) 4, new Point(0, 1),
            (byte) 5, new Point(1, 1),
            (byte) 6, new Point(2, 1),
            (byte) 7, new Point(0, 2),
            (byte) 8, new Point(1, 2),
            (byte) 9, new Point(2, 2),
            (byte) 0, new Point(1, 3)
    );

    public static void callNumber(String number) {
        int[] digits = number.chars().map(c -> c - '0').toArray();

        // Open phone contacts
        keyPress("UP", 10);
        sleep(625);

        keyPress("UP", 10);
        sleep(50);

        keyPress("RIGHT", 10);
        sleep(50);

        keyPress("ENTER", 10);
        sleep(100);

        // Dial number
        keyPress("SPACE", 10);
        sleep(75);
        
        int currentX = 0, currentY = 0;

        for (int digit : digits) {
            Point target = NUMBER_POSITIONS.get((byte) digit);

            if(target == null)
                continue;

            if(target.x != currentX) {
                for (int i = 0; i < Math.abs(target.x - currentX); i++) {
                    keyPress((target.x < currentX) ? "LEFT" : "RIGHT", 10);
                    sleep(50);
                }

                currentX = target.x;
            }

            if(target.y != currentY) {
                for (int i = 0; i < Math.abs(target.y - currentY); i++) {
                    keyPress((target.y < currentY) ? "UP" : "DOWN", 10);
                    sleep(50);
                }

                currentY = target.y;
            }

            keyPress("ENTER", 10);
            sleep(50);
        }

        keyPress("SPACE", 10);
        sleep(50);
    }

}
