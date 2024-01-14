package dev.justix.gtavtools;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import dev.justix.gtavtools.config.ApplicationConfig;
import dev.justix.gtavtools.gui.GUI;
import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.ToolManager;
import dev.justix.gtavtools.util.SystemUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class GTAVTools {

    private static final Logger logger = new Logger(System.out);
    @Getter
    private static GUI gui;
    @Getter
    private static ToolManager toolManager;
    private static final AtomicBoolean ctrlPressed = new AtomicBoolean(false);

    public static void main(String[] args) {
        Thread.currentThread().setName("Application");

        // Set debug mode
        SystemUtil.DEBUG = Arrays.asList(args).contains("--debug");

        // Add shutdown hook to configuration
        ApplicationConfig.addShutdownHook();

        // Initialize GUI
        gui = new GUI();

        // Initialize Tools
        toolManager = new ToolManager(logger);

        // Register Native Hook
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            logger.log(Level.SEVERE, "Could not register native hook: " + ex.getMessage());
            return;
        }

        // Register Native Hook listeners

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
                int rawKeyCode = nativeEvent.getRawCode();
                String keyName = SystemUtil.getKeyName(rawKeyCode);

                if (ctrlPressed.get() && keyName.equals("SCROLL_LOCK")) {
                    Thread.currentThread().setName("Application");
                    logger.log(Level.INFO, "Shutting down application...");

                    gui.close();
                    toolManager.forceStop();

                    try {
                        GlobalScreen.unregisterNativeHook();
                    } catch (NativeHookException ex) {
                        logger.log(Level.SEVERE, "Could not unregister native hook: " + ex.getMessage());
                    }

                    System.exit(0);
                    return;
                } else if (keyName.equals("L_CONTROL"))
                    ctrlPressed.set(true);

                // Open GUI
                if (keyName.equals("R_SHIFT"))
                    gui.open();
                else
                    gui.handleKeyInput(keyName);
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
                if (SystemUtil.getKeyName(nativeEvent.getRawCode()).equals("L_CONTROL"))
                    ctrlPressed.set(false);
            }
        });

        logger.log(Level.INFO, "Initialization complete");
    }

}