package dev.justix.gtavtools.tools.mission;

import dev.justix.gtavtools.config.ApplicationConfig;
import dev.justix.gtavtools.gui.components.settings.BooleanSetting;
import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;
import dev.justix.gtavtools.util.OCRUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static dev.justix.gtavtools.util.SystemUtil.*;

public class ReplayGlitch extends Tool {

    private boolean matched, cancel;

    public ReplayGlitch(Logger logger) {
        super(logger, Category.MISSION, "Replay Glitch");

        addSetting(new BooleanSetting(this, "Elite", true));
        addSetting(new BooleanSetting(this, "Cayo Perico", true));
        addSetting(new BooleanSetting(this, "VIP Contract", false));

        this.relativeData.addRect("1920x1200", "cayo_perico_glitch", 1524, 1097, 19, 17);
        this.relativeData.addRect("1920x1200", "vip_contract_text", 132, 400, 44, 50);
        this.relativeData.addRect("1920x1200", "completed_text", 0, 215, 100, 150);

        this.relativeData.addRect("1920x1080", "cayo_perico_glitch", 1618, 1037, 19, 17);
        this.relativeData.addRect("1920x1080", "vip_contract_text", 40, 342, 44, 52);
        this.relativeData.addRect("1920x1080", "completed_text", 77, 260, 98, 127);
    }

    @Override
    public void execute() {
        this.cancel = false;
        this.matched = false;

        logger.log(Level.INFO, "Waiting for mission completion...");

        try {
            final boolean cayoPerico = booleanValue("Cayo Perico", true);
            final boolean vipContract = booleanValue("VIP Contract", false);
            final AtomicReference<BufferedImage> screenshot = new AtomicReference<>();
            final Object screenshotLock = new Object();

            // take screenshots
            new Thread(() -> {
                final String key = cayoPerico ? "cayo_perico_glitch" : (vipContract ? "vip_contract_text" : "completed_text");
                final Rectangle rect = this.relativeData.getRect(key);

                while (!this.cancel && !this.matched) {
                    screenshot.set(screenshot(rect));

                    synchronized (screenshotLock) {
                        screenshotLock.notify();
                    }
                }
            }).start();

            // check text
            while (!this.cancel && !this.matched) {
                synchronized (screenshotLock) {
                    screenshotLock.wait();
                }

                this.matched = OCRUtil.ocr(screenshot.get(), true)
                        .equals(cayoPerico ? "Tr" : (vipContract ? "MI" : "RA"));
            }

            if (this.cancel)
                return;

            logger.log(Level.INFO, "Performing replay glitch at " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

            if (!DEBUG) {
                sleep(booleanValue("Elite", true) ? 150 : (cayoPerico ? 50 : 5));

                // Disable network
                Runtime.getRuntime().exec(new String[] { "pssuspend", "GTA5.exe" }).waitFor();
                Runtime.getRuntime().exec(new String[] {
                        "netsh",
                        "interface",
                        "set",
                        "interface",
                        String.format("\"%s\"", ApplicationConfig.CONFIG.get("networkInterfaceName")),
                        "disable"
                }).waitFor();
                Runtime.getRuntime().exec(new String[] { "pssuspend", "-r", "GTA5.exe" }).waitFor();

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

                logger.log(Level.INFO, "Glitch completed, connecting back to GTA Online");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An error occurred while executing tool: " + ex.getMessage());
        }
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
