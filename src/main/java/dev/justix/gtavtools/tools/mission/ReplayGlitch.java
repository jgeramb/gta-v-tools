package dev.justix.gtavtools.tools.mission;

import dev.justix.gtavtools.gui.components.settings.BooleanSetting;
import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.ImageUtil;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;
import dev.justix.gtavtools.util.OCRUtil;
import dev.justix.gtavtools.util.SystemUtil;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class ReplayGlitch extends Tool {

    private static final float REQUIRED_MATCH_PERCENTAGE = 0.915f;
    private final BufferedImage requiredPosition;
    private boolean waitingForPosition, instantGlitch, cancel;

    public ReplayGlitch(Logger logger) {
        super(logger, Category.MISSION, "Replay Glitch");

        addSetting(new BooleanSetting(this, "Elite", true));
        addSetting(new BooleanSetting(this, "Match Text", false));

        BufferedImage requiredPositionImage = null;

        try {
            requiredPositionImage = comparableImage(ImageUtil.fromResource("/mission-images/cayo-perico-glitch-position.png"));
        } catch (IOException ignore) {
        }

        this.requiredPosition = requiredPositionImage;
        this.waitingForPosition = false;
        this.instantGlitch = false;
        this.cancel = false;
    }

    @Override
    public void execute() {
        if (waitingForPosition) {
            logger.log(Level.INFO, "Skipping waiting for position...");

            instantGlitch = true;
        } else {
            instantGlitch = false;
            waitingForPosition = true;

            logger.log(Level.INFO, "Waiting for mission completion...");

            try {
                final boolean matchText = booleanValue("Match Text", false);
                final AtomicReference<BufferedImage> currentScreenPart = new AtomicReference<>();
                final Object imageLock = new Object();
                Thread imageThread = null;

                if (!(matchText)) {
                    if (requiredPosition == null)
                        return;

                    imageThread = new Thread(() -> {
                        while (waitingForPosition) {
                            currentScreenPart.set(comparableImage(SystemUtil.screenshot(1176, 432, 220, 464)));

                            synchronized (imageLock) {
                                imageLock.notify();
                            }
                        }
                    });
                    imageThread.start();
                }

                int frames = 0;
                long secondStart = System.currentTimeMillis();

                while (!(instantGlitch || cancel)) {
                    boolean matched;
                    double matchPercentage = 0;

                    if (matchText) {
                        matched = OCRUtil.ocr(SystemUtil.screenshot(0, 218, 617, 145)).equals("RAUBÜBERFALL")
                                || OCRUtil.ocr(SystemUtil.screenshot(132, 400, 160, 50)).equals("MISSION");
                    } else {
                        synchronized (imageLock) {
                            imageLock.wait();
                        }

                        BufferedImage screenPart = currentScreenPart.get();

                        // Compare images
                        matchPercentage = ImageUtil.getMaxMatchPercentage(requiredPosition, screenPart, REQUIRED_MATCH_PERCENTAGE, 3, 3);
                        matched = matchPercentage >= REQUIRED_MATCH_PERCENTAGE;

                        currentScreenPart.set(null);

                        if (SystemUtil.DEBUG && (matchPercentage >= (REQUIRED_MATCH_PERCENTAGE - 0.025)))
                            System.out.printf(Locale.US, "%d%% position similarity\n", Math.round(matchPercentage * 100));

                        if (SystemUtil.DEBUG) {
                            frames++;

                            if ((System.currentTimeMillis() - secondStart) >= 1000L) {
                                System.out.println("FPS: " + frames);
                                frames = 0;
                                secondStart = System.currentTimeMillis();
                            }
                        }
                    }

                    if (matched) {
                        logger.log(Level.INFO, "Mission completed (" + (matchText ? "text" : ("position " + (Math.round(matchPercentage * 100 * 10d) / 10d) + "%")) + " matched), performing replay glitch...");

                        if (!matchText)
                            SystemUtil.sleep(booleanValue("Elite", true) ? 760 : 260);
                        break;
                    }
                }

                if (cancel)
                    return;

                if (imageThread != null)
                    imageThread.interrupt();

                waitingForPosition = false;

                if (!(SystemUtil.DEBUG)) {
                    // Disable network
                    Runtime.getRuntime().exec(new String[]{
                            "C:\\Windows\\System32\\netsh.exe",
                            "interface",
                            "set",
                            "interface",
                            "Intel - 1 Gbit",
                            "disable"
                    }).waitFor();

                    SystemUtil.sleep(16750L);
                    SystemUtil.keyPress("ENTER", 50L);

                    // Reconnect network
                    SystemUtil.sleep(5000L);

                    Runtime.getRuntime().exec(new String[]{
                            "C:\\Windows\\System32\\netsh.exe",
                            "interface",
                            "set",
                            "interface",
                            "Intel - 1 Gbit",
                            "enable"
                    }).waitFor();

                    SystemUtil.sleep(15000L);

                    // Open Social Club
                    SystemUtil.keyPress("HOME", 50L);
                    SystemUtil.sleep(1250L);

                    // Reconnect and close pop-up
                    SystemUtil.robot().mouseMove(1210, 334);
                    SystemUtil.sleep(150L);
                    SystemUtil.mouseClick("LEFT", 100L);
                    SystemUtil.sleep(5000L);

                    // Close pop-up
                    SystemUtil.keyPress("ESCAPE", 50L);
                    SystemUtil.sleep(3500L);

                    InterfaceNavigationUtil.openPlayOnlineOptions(true);

                    // Select 'Invite-only session'
                    SystemUtil.keyPress("DOWN", 50L);
                    SystemUtil.sleep(150L);
                    SystemUtil.keyPress("ENTER", 50L);
                    SystemUtil.sleep(750L);

                    // Accept warning
                    SystemUtil.keyPress("ENTER", 50L);

                    logger.log(Level.INFO, "Glitch completed, connecting back to GTA Online");
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "An error occurred while executing tool: " + ex.getMessage());
            }
        }
    }

    @Override
    public void forceStop() {
        cancel = true;
    }

    private BufferedImage comparableImage(BufferedImage image) {
        new RescaleOp(3.3f, 80, null).filter(image, image);

        return ImageUtil.transform(image, true);
    }

}
