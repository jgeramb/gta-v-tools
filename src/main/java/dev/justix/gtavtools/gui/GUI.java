package dev.justix.gtavtools.gui;

import dev.justix.gtavtools.GTAVTools;
import dev.justix.gtavtools.gui.components.Component;
import dev.justix.gtavtools.gui.components.View;
import dev.justix.gtavtools.gui.components.settings.BooleanSetting;
import dev.justix.gtavtools.gui.components.settings.DoubleSetting;
import dev.justix.gtavtools.gui.components.settings.IntegerSetting;
import dev.justix.gtavtools.gui.components.settings.Setting;
import dev.justix.gtavtools.gui.views.CategoryView;
import dev.justix.gtavtools.gui.views.MainView;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.SystemUtil;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame {

    private static final int
            ALPHA = 245,
            WIDTH = 270,
            HEADER_HEIGHT = 70,
            COMPONENT_HEIGHT = 40,
            INSET = 15;
    private static final Color
            COMPONENT_COLOR = new Color(26, 33, 42, ALPHA),
            SELECTED_COMPONENT_COLOR = new Color(63, 73, 87, ALPHA),
            TEXT_COLOR = new Color(245, 245, 248);
    private static final Font
            TITLE_FONT = new Font("Impact", Font.BOLD, 32),
            COMPONENT_FONT = new Font("Arial", Font.PLAIN, 22);

    @Getter
    private final MainView mainView;
    private boolean open;
    private View currentView;

    public GUI() {
        super("GTA5-Tools");

        this.mainView = new MainView();
        this.open = false;
        this.currentView = mainView;

        // Window settings
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setResizable(false);

        // Window style
        setSize(WIDTH + (2 * INSET), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
        setUndecorated(true);
        setType(Type.UTILITY);

        setBackground(new Color(0f, 0f, 0f, 0.25f));
        getContentPane().setBackground(new Color(0f, 0f, 0f, 0f));
        getContentPane().setLayout(null);
    }

    public void open() {
        open = true;

        // Show window & focus
        setVisible(true);
        setAlwaysOnTop(true);

        SystemUtil.sleep(200);
        SystemUtil.robot().mouseMove(getWidth() / 2, getHeight() / 2);
        SystemUtil.mouseClick("LEFT", 15);
    }

    public void close() {
        open = false;

        // Close window and focus other application
        setVisible(false);
        setAlwaysOnTop(false);

        SystemUtil.sleep(25);

        Dimension screenBounds = Toolkit.getDefaultToolkit().getScreenSize();
        SystemUtil.robot().mouseMove(screenBounds.width / 2, screenBounds.height / 2);
        SystemUtil.mouseClick("LEFT", 5);
        SystemUtil.sleep(150);
    }

    @Override
    public void paintComponents(Graphics rawGraphics) {
        Graphics2D graphics = (Graphics2D) rawGraphics;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw header
        graphics.setColor(new Color(146, 85, 195, ALPHA));
        graphics.fillRect(INSET, INSET, WIDTH, HEADER_HEIGHT);
        graphics.setColor(TEXT_COLOR);

        String title = (currentView instanceof Tool) ? "Tool Settings" : currentView.getName();

        if (title == null)
            title = "GTAV Tools";

        drawCenteredStringInRect(graphics, TITLE_FONT, title.toUpperCase(), INSET + 25, INSET, INSET + WIDTH - 25, INSET + HEADER_HEIGHT);

        // Draw components
        int currentY = INSET + HEADER_HEIGHT, currentComponent = 0;

        if (currentView instanceof Tool tool) {
            for (Setting setting : tool.getComponents()) {
                graphics.setColor((currentComponent == tool.getCurrentIndex()) ? SELECTED_COMPONENT_COLOR : COMPONENT_COLOR);
                graphics.fillRect(INSET, currentY, WIDTH, COMPONENT_HEIGHT);
                graphics.setColor(TEXT_COLOR);
                graphics.setFont(COMPONENT_FONT);

                int componentEndY = currentY + COMPONENT_HEIGHT, stringHeight = graphics.getFontMetrics(COMPONENT_FONT).getHeight();
                int y = Math.min(currentY, componentEndY) + ((Math.abs(componentEndY - currentY) + stringHeight) / 2);
                y = Math.min(y, Math.max(currentY, componentEndY) - (stringHeight / 2));
                y = Math.max(y, Math.min(currentY, componentEndY));

                graphics.drawString(setting.getName(), INSET + 10, y);

                String valueText = null;

                if (setting instanceof BooleanSetting booleanSetting)
                    valueText = booleanSetting.booleanValue() ? "On" : "Off";
                else if (setting instanceof IntegerSetting integerSetting)
                    valueText = integerSetting.integerValue().toString();
                else if (setting instanceof DoubleSetting doubleSetting)
                    valueText = doubleSetting.doubleValue().toString();

                if (valueText != null)
                    graphics.drawString(valueText, INSET + WIDTH - 10 - graphics.getFontMetrics(COMPONENT_FONT).stringWidth(valueText), y);

                currentY += COMPONENT_HEIGHT;
                currentComponent++;
            }
        } else {
            for (dev.justix.gtavtools.gui.components.Component component : currentView.getComponents()) {
                drawComponent(graphics, component.getName(), currentView.getCurrentIndex() == currentComponent, currentY);

                currentY += COMPONENT_HEIGHT;
                currentComponent++;
            }
        }
    }

    @Override
    public void paint(Graphics rawGraphics) {
        Graphics2D graphics = (Graphics2D) rawGraphics;

        // Transparent window
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        graphics.setColor(getBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());

        paintComponents(rawGraphics);
    }

    private void drawComponent(Graphics2D graphics, String text, boolean selected, int y) {
        graphics.setColor(selected ? SELECTED_COMPONENT_COLOR : COMPONENT_COLOR);
        graphics.fillRect(INSET, y, WIDTH, COMPONENT_HEIGHT);
        graphics.setColor(TEXT_COLOR);
        drawCenteredStringInRect(graphics, COMPONENT_FONT, text, INSET + 10, y, INSET + WIDTH - 10, y + COMPONENT_HEIGHT);
    }

    private void drawCenteredStringInRect(Graphics2D graphics, Font font, String string, int x1, int y1, int x2, int y2) {
        int stringHeight = graphics.getFontMetrics(font).getHeight();
        int y = Math.min(y1, y2) + ((Math.abs(y2 - y1) + stringHeight) / 2);
        y = Math.min(y, Math.max(y1, y2) - (stringHeight / 2));
        y = Math.max(y, Math.min(y1, y2));

        drawCenteredString(graphics, font, string, x1, x2, y);
    }

    private void drawCenteredString(Graphics2D graphics, Font font, String string, int x1, int x2, int y) {
        int stringWidth = graphics.getFontMetrics(font).stringWidth(string);
        int x = Math.min(x1, x2) + ((Math.abs(x2 - x1) - stringWidth) / 2);
        x = Math.min(x, Math.max(x1, x2) - stringWidth);
        x = Math.max(x, Math.min(x1, x2));

        graphics.setFont(font);
        graphics.drawString(string, x, y);
    }

    public void handleKeyInput(String key) {
        if (!(open))
            return;

        switch (key) {
            case "ESCAPE" -> {
                if (currentView.getParent() == null)
                    close();
                else {
                    currentView.setCurrentIndex(0);

                    setCurrentView(currentView.getParent());
                }
            }
            case "TAB" -> {
                if (currentView instanceof CategoryView categoryView) {
                    Category.byDisplayName(categoryView.getName()).ifPresent(category -> {
                        close();
                        GTAVTools.getToolManager().executeTool(categoryView.getComponents().get(categoryView.getCurrentIndex()));
                    });
                }
            }
            case "ENTER" -> {
                Component component = currentView.getComponents().get(currentView.getCurrentIndex());
                if (component instanceof View view) {
                    if (!(view.getComponents().isEmpty()))
                        setCurrentView(view);
                }
            }
            case "DOWN" -> {
                if (currentView.getCurrentIndex() == (currentView.getComponents().size() - 1))
                    currentView.setCurrentIndex(0);
                else
                    currentView.setCurrentIndex(currentView.getCurrentIndex() + 1);

                repaint();
            }
            case "UP" -> {
                if (currentView.getCurrentIndex() == 0)
                    currentView.setCurrentIndex(currentView.getComponents().size() - 1);
                else
                    currentView.setCurrentIndex(currentView.getCurrentIndex() - 1);

                repaint();
            }
            case "RIGHT" -> {
                if (currentView instanceof Tool tool) {
                    tool.getComponents().get(tool.getCurrentIndex()).increase();
                    repaint();
                }
            }
            case "LEFT" -> {
                if (currentView instanceof Tool tool) {
                    tool.getComponents().get(tool.getCurrentIndex()).decrease();
                    repaint();
                }
            }
        }
    }

    public void setCurrentView(View view) {
        this.currentView = view;

        repaint();
    }

}
