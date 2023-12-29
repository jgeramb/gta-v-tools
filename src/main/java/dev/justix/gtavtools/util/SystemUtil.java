package dev.justix.gtavtools.util;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

public class SystemUtil {

    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("app.debug", "false"));

    public static final String RESOLUTION;
    private static final long SLEEP_PRECISION, SPIN_YIELD_PRECISION;
    private static final Robot ROBOT;

    static {
        final Dimension screenBounds = Toolkit.getDefaultToolkit().getScreenSize();
        RESOLUTION = screenBounds.width + "x" + screenBounds.height;

        SLEEP_PRECISION = TimeUnit.MILLISECONDS.toNanos(2);
        SPIN_YIELD_PRECISION = TimeUnit.MILLISECONDS.toNanos(2);

        Robot robot;

        try {
            robot = new Robot();
        } catch (AWTException ignore) {
            robot = null;
        }

        ROBOT = robot;
    }

    public static BufferedImage screenshot(int x, int y, int width, int height) {
        return screenshot(new Rectangle(x, y, width, height));
    }

    public static BufferedImage screenshot(Rectangle rectangle) {
        return ROBOT.createScreenCapture(rectangle);
    }

    public static Color getScreenPixelColor(int x, int y) {
        return ROBOT.getPixelColor(x, y);
    }

    public static void keyPress(String keyName, long duration) {
        int keyCode = getKeyboardCode(keyName);

        if (keyCode == -1) return;

        try {
            ROBOT.keyPress(keyCode);
        } catch (IllegalArgumentException ignore) {
            System.err.println("Key '" + keyName + "' has an invalid key code");
            return;
        }

        new Thread(() -> {
            sleep(duration);

            ROBOT.keyRelease(keyCode);
        }).start();
    }

    public static void mouseClick(String buttonName, long duration) {
        int keyCode = getMouseCode(buttonName);

        ROBOT.mousePress(keyCode);

        new Thread(() -> {
            sleep(duration);

            ROBOT.mouseRelease(keyCode);
        }).start();
    }

    public static void sleep(long millis) {
        sleepNanos(TimeUnit.MILLISECONDS.toNanos(millis));
    }

    private static void sleepNanos(long duration) {
        final long end = System.nanoTime() + duration;
        long timeLeft = duration;

        do {
            if (timeLeft > SLEEP_PRECISION) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignore) {
                }
            } else if (timeLeft > SPIN_YIELD_PRECISION)
                Thread.onSpinWait();

            timeLeft = end - System.nanoTime();

            if (Thread.interrupted()) break;
        } while (timeLeft > 0);
    }

    public static int getKeyboardCode(String keyName) {
        return switch (keyName) {
            // Row 1
            case "ESCAPE" -> KeyEvent.VK_ESCAPE;
            case "F1" -> KeyEvent.VK_F1;
            case "F2" -> KeyEvent.VK_F2;
            case "F3" -> KeyEvent.VK_F3;
            case "F4" -> KeyEvent.VK_F4;
            case "F5" -> KeyEvent.VK_F5;
            case "F6" -> KeyEvent.VK_F6;
            case "F7" -> KeyEvent.VK_F7;
            case "F8" -> KeyEvent.VK_F8;
            case "F9" -> KeyEvent.VK_F9;
            case "F10" -> KeyEvent.VK_F10;
            case "F11" -> KeyEvent.VK_F11;
            case "F12" -> KeyEvent.VK_F12;
            case "PRINT" -> KeyEvent.VK_PRINTSCREEN;
            case "SCROLL_LOCK" -> KeyEvent.VK_SCROLL_LOCK;
            case "PAUSE" -> KeyEvent.VK_PAUSE;
            // Row 2
            case "CIRCUMFLEX" -> KeyEvent.VK_DEAD_CIRCUMFLEX;
            case "1" -> KeyEvent.VK_1;
            case "2" -> KeyEvent.VK_2;
            case "3" -> KeyEvent.VK_3;
            case "4" -> KeyEvent.VK_4;
            case "5" -> KeyEvent.VK_5;
            case "6" -> KeyEvent.VK_6;
            case "7" -> KeyEvent.VK_7;
            case "8" -> KeyEvent.VK_8;
            case "9" -> KeyEvent.VK_9;
            case "0" -> KeyEvent.VK_0;
            case "ACUTE" -> KeyEvent.VK_DEAD_ACUTE;
            case "BACKSPACE" -> KeyEvent.VK_BACK_SPACE;
            case "INSERT" -> KeyEvent.VK_INSERT;
            case "HOME" -> KeyEvent.VK_HOME;
            case "PAGE_UP" -> KeyEvent.VK_PAGE_UP;
            case "NUM_LOCK" -> KeyEvent.VK_NUM_LOCK;
            case "DIVIDE" -> KeyEvent.VK_DIVIDE;
            case "MULTIPLY" -> KeyEvent.VK_MULTIPLY;
            case "MINUS" -> KeyEvent.VK_SUBTRACT;
            // Row 3
            case "TAB" -> KeyEvent.VK_TAB;
            case "Q" -> KeyEvent.VK_Q;
            case "W" -> KeyEvent.VK_W;
            case "E" -> KeyEvent.VK_E;
            case "R" -> KeyEvent.VK_R;
            case "T" -> KeyEvent.VK_T;
            case "Z" -> KeyEvent.VK_Z;
            case "U" -> KeyEvent.VK_U;
            case "I" -> KeyEvent.VK_I;
            case "O" -> KeyEvent.VK_O;
            case "P" -> KeyEvent.VK_P;
            case "PLUS" -> KeyEvent.VK_PLUS;
            case "ENTER" -> KeyEvent.VK_ENTER;
            case "DELETE" -> KeyEvent.VK_DELETE;
            case "END" -> KeyEvent.VK_END;
            case "PAGE_DOWN" -> KeyEvent.VK_PAGE_DOWN;
            case "NUM_PLUS" -> KeyEvent.VK_ADD;
            // Row 4
            case "CAPS_LOCK" -> KeyEvent.VK_COMPOSE;
            case "A" -> KeyEvent.VK_A;
            case "S" -> KeyEvent.VK_S;
            case "D" -> KeyEvent.VK_D;
            case "F" -> KeyEvent.VK_F;
            case "G" -> KeyEvent.VK_G;
            case "H" -> KeyEvent.VK_H;
            case "J" -> KeyEvent.VK_J;
            case "K" -> KeyEvent.VK_K;
            case "L" -> KeyEvent.VK_L;
            case "HASHTAG" -> KeyEvent.VK_NUMBER_SIGN;
            // Row 5
            case "L_SHIFT" -> KeyEvent.VK_SHIFT;
            case "GRAVE" -> KeyEvent.VK_LESS;
            case "Y" -> KeyEvent.VK_Y;
            case "X" -> KeyEvent.VK_X;
            case "C" -> KeyEvent.VK_C;
            case "V" -> KeyEvent.VK_V;
            case "B" -> KeyEvent.VK_B;
            case "N" -> KeyEvent.VK_N;
            case "M" -> KeyEvent.VK_M;
            case "COMMA" -> KeyEvent.VK_COMMA;
            case "DOT" -> KeyEvent.VK_PERIOD;
            case "DASH" -> KeyEvent.VK_MINUS;
            case "R_SHIFT" -> KeyEvent.VK_SHIFT;
            case "UP" -> KeyEvent.VK_UP;
            // Row 6
            case "L_CONTROL", "R_CONTROL" -> KeyEvent.VK_CONTROL;
            case "L_ALT", "R_ALT" -> KeyEvent.VK_ALT;
            case "L_META", "R_META" -> KeyEvent.VK_WINDOWS;
            case "SPACE" -> KeyEvent.VK_SPACE;
            case "LEFT" -> KeyEvent.VK_LEFT;
            case "DOWN" -> KeyEvent.VK_DOWN;
            case "RIGHT" -> KeyEvent.VK_RIGHT;
            // Fallback
            default -> -1;
        };
    }

    public static String getKeyName(int rawKeyCode) {
        return switch (rawKeyCode) {
            // Row 1
            case 27 -> "ESCAPE";
            case 112 -> "F1";
            case 113 -> "F2";
            case 114 -> "F3";
            case 115 -> "F4";
            case 116 -> "F5";
            case 117 -> "F6";
            case 118 -> "F7";
            case 119 -> "F8";
            case 120 -> "F9";
            case 121 -> "F10";
            case 122 -> "F11";
            case 123 -> "F12";
            case 44 -> "PRINT";
            case 145, 3 -> "SCROLL_LOCK";
            case 19 -> "PAUSE";
            case 179 -> "PAUSE_PLAY";
            // Row 2
            case 226 -> "CIRCUMFLEX";
            case 49 -> "1";
            case 50 -> "2";
            case 51 -> "3";
            case 52 -> "4";
            case 53 -> "5";
            case 54 -> "6";
            case 55 -> "7";
            case 56 -> "8";
            case 57 -> "9";
            case 48 -> "0";
            case 219 -> "ß";
            case 221 -> "ACUTE";
            case 8 -> "BACKSPACE";
            case 45 -> "INSERT";
            case 36 -> "HOME";
            case 33 -> "PAGE_UP";
            case 144 -> "NUM_LOCK";
            case 111 -> "DIVIDE";
            case 106 -> "MULTIPLY";
            case 109 -> "MINUS";
            // Row 3
            case 9 -> "TAB";
            case 81 -> "Q";
            case 87 -> "W";
            case 69 -> "E";
            case 82 -> "R";
            case 84 -> "T";
            case 90 -> "Z";
            case 85 -> "U";
            case 73 -> "I";
            case 79 -> "O";
            case 80 -> "P";
            case 186 -> "Ü";
            case 187 -> "PLUS";
            case 13 -> "ENTER";
            case 46 -> "DELETE";
            case 35 -> "END";
            case 34 -> "PAGE_DOWN";
            case 107 -> "NUM_PLUS";
            // Row 4
            case 20 -> "CAPS_LOCK";
            case 65 -> "A";
            case 83 -> "S";
            case 68 -> "D";
            case 70 -> "F";
            case 71 -> "G";
            case 72 -> "H";
            case 74 -> "J";
            case 75 -> "K";
            case 76 -> "L";
            case 192 -> "Ö";
            case 222 -> "Ä";
            case 191 -> "HASHTAG";
            // Row 5
            case 160 -> "L_SHIFT";
            case 220 -> "GRAVE";
            case 89 -> "Y";
            case 88 -> "X";
            case 67 -> "C";
            case 86 -> "V";
            case 66 -> "B";
            case 78 -> "N";
            case 77 -> "M";
            case 188 -> "COMMA";
            case 190 -> "DOT";
            case 189 -> "DASH";
            case 161 -> "R_SHIFT";
            case 38 -> "UP";
            // Row 6
            case 162 -> "L_CONTROL";
            case 164 -> "L_ALT";
            case 91 -> "L_META";
            case 32 -> "SPACE";
            case 92 -> "R_META";
            case 165 -> "R_ALT";
            case 163 -> "R_CONTROL";
            case 37 -> "LEFT";
            case 40 -> "DOWN";
            case 39 -> "RIGHT";
            // Fallback
            default -> "UNKNOWN";
        };
    }

    public static int getMouseCode(String buttonName) {
        return switch (buttonName) {
            case "RIGHT" -> KeyEvent.BUTTON3_DOWN_MASK;
            case "MIDDLE" -> KeyEvent.BUTTON2_DOWN_MASK;
            default -> KeyEvent.BUTTON1_DOWN_MASK;
        };
    }

    public static String getButtonName(int button) {
        return switch (button) {
            case 2 -> "RIGHT";
            case 3 -> "MIDDLE";
            default -> "LEFT";
        };
    }

    public static Robot robot() {
        return ROBOT;
    }

}
