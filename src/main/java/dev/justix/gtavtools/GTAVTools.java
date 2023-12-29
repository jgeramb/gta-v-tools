package dev.justix.gtavtools;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import dev.justix.gtavtools.gui.GUI;
import dev.justix.gtavtools.input.Input;
import dev.justix.gtavtools.input.InputType;
import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.ToolManager;
import dev.justix.gtavtools.util.SystemUtil;
import lombok.Getter;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GTAVTools {

    private static final Logger logger = new Logger(System.out);
    private static final List<Input> inputs = new ArrayList<>();
    @Getter
    private static GUI gui;
    @Getter
    private static ToolManager toolManager;
    private static final AtomicBoolean ctrlPressed = new AtomicBoolean(false);
    private static long lastActionTime;

    public static void main(String[] args) throws InterruptedException {
        final boolean recordInputs = (args.length == 0) || !(args[0].equalsIgnoreCase("--no-record"));
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenBounds = toolkit.getScreenSize();
        final int centerX = screenBounds.width / 2, centerY = screenBounds.height / 2;

        Thread.currentThread().setName("Application");

        // Initialize GUI
        gui = new GUI();

        // Initialize Tools
        toolManager = new ToolManager(logger);

        // Prepare for input recording
        if (recordInputs) {
            // Small cooldown to give the user time to switch applications
            logger.log(Level.INFO, "Application started, initializing in 3 seconds...");

            Thread.sleep(3 * 1000L);

            // Move mouse to center of screen
            SystemUtil.robot().mouseMove(centerX, centerY);

            logger.log(Level.INFO, "Mouse moved to the center of the screen");
        }

        // Register Native Hook
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            logger.log(Level.SEVERE, "Could not register native hook: " + ex.getMessage());
            return;
        }

        // Register Native Hook listeners

        lastActionTime = System.currentTimeMillis();

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
                long timeDiff = System.currentTimeMillis() - lastActionTime;

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

                    if (recordInputs) {
                        logger.log(Level.INFO, "Generating code...");

                        StringBuilder code = new StringBuilder();
                        long lastDuration = -1;

                        for (int i = 0; i < inputs.size() - 1; i++) {
                            Input input = inputs.get(i);

                            if (input.getType().equals(InputType.KEY_DOWN)) {
                                int key = (int) input.getValue();
                                long time = 0;

                                for (int j = i + 1; j < inputs.size(); j++) {
                                    Input tmpInput = inputs.get(j);

                                    if (tmpInput.getType().equals(InputType.KEY_UP) && (key == tmpInput.getValue())) {
                                        code.append("keyPress(\"").append(SystemUtil.getKeyName(key)).append("\", ").append(time).append("L);").append(System.lineSeparator());
                                        break;
                                    } else if (tmpInput.getType().equals(InputType.WAIT))
                                        time += tmpInput.getValue();
                                }

                                lastDuration = time;
                            } else if (input.getType().equals(InputType.MOUSE_DOWN)) {
                                int button = (int) input.getValue();
                                long time = 0;

                                for (int j = i + 1; j < inputs.size(); j++) {
                                    Input tmpInput = inputs.get(j);

                                    if (tmpInput.getType().equals(InputType.MOUSE_UP) && (button == tmpInput.getValue())) {
                                        code.append("mouseClick(\"").append(SystemUtil.getButtonName(button)).append("\", ").append(time).append("L);").append(System.lineSeparator());
                                        break;
                                    } else if (tmpInput.getType().equals(InputType.WAIT))
                                        time += tmpInput.getValue();
                                }

                                lastDuration = time;
                            } else if (input.getType().equals(InputType.WAIT) && (lastDuration != -1)) {
                                long time = input.getValue();

                                if (time != lastDuration) {
                                    code.append("sleep(").append(time).append("L);").append(System.lineSeparator());

                                    lastDuration = -1;
                                }
                            }
                        }

                        logger.log(Level.INFO, "Copying code to clipboard...");

                        try {
                            System.out.println("\n" + code);

                            StringSelection selection = new StringSelection(code.toString());
                            toolkit.getSystemClipboard().setContents(selection, null);
                        } catch (Throwable ignored) {
                        }
                    }

                    System.exit(0);
                    return;
                } else if (keyName.equals("PAUSE"))
                    System.exit(0);
                else {
                    if (recordInputs) {
                        if (timeDiff > 0)
                            inputs.add(new Input(InputType.WAIT, timeDiff));

                        inputs.add(new Input(InputType.KEY_DOWN, rawKeyCode));

                        lastActionTime = System.currentTimeMillis();
                    }

                    if (keyName.equals("L_CONTROL"))
                        ctrlPressed.set(true);
                }

                // Open GUI
                if (keyName.equals("R_SHIFT"))
                    gui.open();
                else
                    gui.handleKeyInput(keyName);
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
                if (recordInputs) {
                    long timeDiff = System.currentTimeMillis() - lastActionTime;

                    if (timeDiff > 0)
                        inputs.add(new Input(InputType.WAIT, timeDiff));

                    int rawKeyCode = nativeEvent.getRawCode();

                    if (SystemUtil.getKeyName(rawKeyCode).equals("L_CONTROL"))
                        ctrlPressed.set(false);

                    inputs.add(new Input(InputType.KEY_UP, rawKeyCode));

                    lastActionTime = System.currentTimeMillis();
                }
            }
        });

        if (recordInputs) {
            GlobalScreen.addNativeMouseListener(new NativeMouseInputListener() {
                @Override
                public void nativeMousePressed(NativeMouseEvent nativeEvent) {
                    long timeDiff = System.currentTimeMillis() - lastActionTime;

                    if (timeDiff > 0)
                        inputs.add(new Input(InputType.WAIT, timeDiff));

                    inputs.add(new Input(InputType.MOUSE_DOWN, nativeEvent.getButton()));

                    lastActionTime = System.currentTimeMillis();
                }

                @Override
                public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
                    long timeDiff = System.currentTimeMillis() - lastActionTime;

                    if (timeDiff > 0)
                        inputs.add(new Input(InputType.WAIT, timeDiff));

                    inputs.add(new Input(InputType.MOUSE_UP, nativeEvent.getButton()));

                    lastActionTime = System.currentTimeMillis();
                }
            });
        }

        logger.log(Level.INFO, "Initialization complete, waiting for inputs...");
    }

}