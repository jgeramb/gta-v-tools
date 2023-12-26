package dev.justix.gtavtools.tools.mission;

import dev.justix.gtavtools.config.ApplicationConfig;
import dev.justix.gtavtools.gui.components.settings.BooleanSetting;
import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.ImageUtil;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;
import dev.justix.gtavtools.util.OCRUtil;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static dev.justix.gtavtools.util.SystemUtil.*;

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
    }

    @Override
    public void execute() {
        this.cancel = false;

        if (this.waitingForPosition) {
            this.logger.log(Level.INFO, "Skipping waiting for position...");

            this.instantGlitch = true;
        } else {
            this.instantGlitch = false;
            this.waitingForPosition = true;

            this.logger.log(Level.INFO, "Waiting for mission completion...");

            try {
                final boolean matchText = booleanValue("Match Text", false);
                final AtomicReference<BufferedImage> currentScreenPart = new AtomicReference<>();
                final Object imageLock = new Object();
                Thread imageThread = null;

                if (!(matchText)) {
                    if (this.requiredPosition == null)
                        return;

                    imageThread = new Thread(() -> {
                        while (this.waitingForPosition) {
                            currentScreenPart.set(comparableImage(screenshot(1176, 432, 220, 464)));

                            synchronized (imageLock) {
                                imageLock.notify();
                            }
                        }
                    });
                    imageThread.start();
                }

                int frames = 0;
                long secondStart = System.currentTimeMillis();

                while (!(this.instantGlitch || this.cancel)) {
                    boolean matched;
                    double matchPercentage = 0;

                    if (matchText) {
                        matched = OCRUtil.ocr(screenshot(0, 218, 617, 145)).equals("RAUBÜBERFALL")
                                || OCRUtil.ocr(screenshot(132, 400, 160, 50)).equals("MISSION");
                    } else {
                        synchronized (imageLock) {
                            imageLock.wait();
                        }

                        BufferedImage screenPart = currentScreenPart.get();

                        // Compare images
                        matchPercentage = ImageUtil.getMaxMatchPercentage(this.requiredPosition, screenPart, REQUIRED_MATCH_PERCENTAGE, 3, 3);
                        matched = matchPercentage >= REQUIRED_MATCH_PERCENTAGE;

                        currentScreenPart.set(null);

                        if (DEBUG && (matchPercentage >= (REQUIRED_MATCH_PERCENTAGE - 0.025)))
                            System.out.printf(Locale.US, "%d%% position similarity\n", Math.round(matchPercentage * 100));

                        if (DEBUG) {
                            frames++;

                            if ((System.currentTimeMillis() - secondStart) >= 1000L) {
                                System.out.println("FPS: " + frames);
                                frames = 0;
                                secondStart = System.currentTimeMillis();
                            }
                        }
                    }

                    if (matched) {
                        this.logger.log(Level.INFO, "Mission completed (" + (matchText ? "text" : ("position " + (Math.round(matchPercentage * 100 * 10d) / 10d) + "%")) + " matched), performing replay glitch...");

                        if (!matchText && !DEBUG)
                            sleep(booleanValue("Elite", true) ? 375 : 255);
                        break;
                    }
                }

                if (this.cancel)
                    return;

                if (imageThread != null)
                    imageThread.interrupt();

                this.waitingForPosition = false;

                if (!(DEBUG)) {
                    // Disable network
                    Runtime.getRuntime().exec(new String[] {
                            "netsh",
                            "interface",
                            "set",
                            "interface",
                            String.format("\"%s\"", ApplicationConfig.CONFIG.get("networkInterfaceName")),
                            "disable"
                    }).waitFor();

                    sleep(16750L);
                    keyPress("ENTER", 50L);

                    // Reconnect network
                    sleep(5000L);

                    Runtime.getRuntime().exec(new String[]{
                            "netsh",
                            "interface",
                            "set",
                            "interface",
                            String.format("\"%s\"", ApplicationConfig.CONFIG.get("networkInterfaceName")),
                            "enable"
                    }).waitFor();

                    sleep(15000L);

                    // Open Social Club
                    keyPress("HOME", 50L);
                    sleep(1250L);

                    // Reconnect and close pop-up
                    robot().mouseMove(1210, 334);
                    sleep(150L);
                    mouseClick("LEFT", 100L);

                    sleep(4000L);

                    // Close pop-up
                    keyPress("ESCAPE", 50L);
                    sleep(3500L);

                    InterfaceNavigationUtil.openPlayOnlineOptions(true);

                    // Select 'Invite-only session'
                    keyPress("DOWN", 50L);
                    sleep(150L);
                    keyPress("ENTER", 50L);
                    sleep(750L);

                    // Accept warning
                    keyPress("ENTER", 50L);

                    this.logger.log(Level.INFO, "Glitch completed, connecting back to GTA Online");
                }
            } catch (Exception ex) {
                this.logger.log(Level.SEVERE, "An error occurred while executing tool: " + ex.getMessage());
            }
        }
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

    private BufferedImage comparableImage(BufferedImage image) {
        new RescaleOp(3.3f, 80, null).filter(image, image);

        return ImageUtil.transform(image, true);
    }

}
