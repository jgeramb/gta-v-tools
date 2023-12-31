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
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.SystemUtil;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Function;

public class GUI extends JFrame {

    private static final int
            ALPHA = 250,
            WIDTH = 220,
            HEADER_HEIGHT = 60,
            COMPONENT_HEIGHT = 32,
            INSET = 16,
            COMPONENT_INSET = 12;
    private static final Color
            COMPONENT_COLOR = new Color(39, 39, 42, ALPHA),
            SELECTED_COMPONENT_COLOR = new Color(63, 63, 70, ALPHA),
            TEXT_COLOR = new Color(245, 245, 248);
    public static final Font TITLE_FONT, COMPONENT_FONT;

    static {
        Font titleFont, componentFont;

        try {
            titleFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(GUI.class.getResourceAsStream("/fonts/Outfit.ttf"))).deriveFont(Font.BOLD, 24f);
            componentFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(GUI.class.getResourceAsStream("/fonts/Inter.ttf"))).deriveFont(16f);
        } catch (FontFormatException | IOException ignore) {
            titleFont = new Font("Impact", Font.BOLD, 24);
            componentFont = new Font("Arial", Font.PLAIN, 16);
        }

        TITLE_FONT = titleFont;
        COMPONENT_FONT = componentFont;
    }

    public static <T> List<T> sortByWidth(Collection<T> entries, Function<T, String> stringFunction) {
        final Canvas canvas = new Canvas();
        final FontMetrics fontMetrics = canvas.getFontMetrics(COMPONENT_FONT);
        final List<T> sortedEntries = new ArrayList<>(entries
                .stream()
                .sorted(Comparator.comparingInt(element -> fontMetrics.stringWidth(stringFunction.apply(element))))
                .toList());

        Collections.reverse(sortedEntries);

        return sortedEntries;
    }

    @Getter
    private final MainView mainView;
    private boolean open;
    private View currentView;
    private List<? extends Component> currentComponents;

    public GUI() {
        super("GTA5-Tools");

        this.mainView = new MainView();
        this.open = false;
        this.currentView = mainView;

        setCurrentComponents(mainView);

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
        this.open = true;

        // Show window & focus
        setVisible(true);
        setAlwaysOnTop(true);

        SystemUtil.sleep(200);
        SystemUtil.robot().mouseMove(getWidth() / 2, getHeight() / 2);
        SystemUtil.mouseClick("LEFT", 15);
    }

    public void close() {
        this.open = false;

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
        graphics.setColor(new Color(24, 24, 27, ALPHA));
        graphics.fillRect(INSET, INSET, WIDTH, HEADER_HEIGHT);
        graphics.setColor(TEXT_COLOR);

        String title = (this.currentView instanceof Tool) ? "Options" : this.currentView.getName();

        if (title == null)
            title = "Tools";

        drawCenteredStringInRect(graphics, TITLE_FONT, title.toUpperCase(), INSET + 25, INSET, INSET + WIDTH - 25, INSET + HEADER_HEIGHT);

        // Draw components
        int currentY = INSET + HEADER_HEIGHT, currentComponent = 0;

        if (this.currentView instanceof Tool tool) {
            for (Component component : this.currentComponents) {
                final Setting setting = (Setting) component;

                graphics.setColor((currentComponent == tool.getCurrentIndex()) ? SELECTED_COMPONENT_COLOR : COMPONENT_COLOR);
                graphics.fillRect(INSET, currentY, WIDTH, COMPONENT_HEIGHT);
                graphics.setColor(TEXT_COLOR);
                graphics.setFont(COMPONENT_FONT);

                int componentEndY = currentY + COMPONENT_HEIGHT, stringHeight = graphics.getFontMetrics(COMPONENT_FONT).getHeight();
                int y = Math.min(currentY, componentEndY) + ((Math.abs(componentEndY - currentY) + stringHeight) / 2);
                y = Math.min(y, Math.max(currentY, componentEndY) - (stringHeight / 2));
                y = Math.max(y, Math.min(currentY, componentEndY));

                graphics.drawString(setting.getName(), INSET + COMPONENT_INSET, y);

                String valueText = null;

                if (setting instanceof BooleanSetting booleanSetting)
                    valueText = booleanSetting.booleanValue() ? "ON" : "OFF";
                else if (setting instanceof IntegerSetting integerSetting)
                    valueText = integerSetting.integerValue().toString();
                else if (setting instanceof DoubleSetting doubleSetting)
                    valueText = doubleSetting.doubleValue().toString();

                if (valueText != null)
                    graphics.drawString(valueText, INSET + WIDTH - COMPONENT_INSET - graphics.getFontMetrics(COMPONENT_FONT).stringWidth(valueText), y);

                currentY += COMPONENT_HEIGHT;
                currentComponent++;
            }
        } else {
            for (Component component : this.currentComponents) {
                drawComponent(graphics, component.getName(), this.currentView.getCurrentIndex() == currentComponent, currentY);

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
        graphics.setFont(COMPONENT_FONT);
        graphics.drawString(text, INSET + COMPONENT_INSET, getCenteredY(graphics, COMPONENT_FONT, y, y + COMPONENT_HEIGHT));
    }

    private int getCenteredY(Graphics2D graphics, Font font, int y1, int y2) {
        final FontMetrics fontMetrics = graphics.getFontMetrics(font);
        int stringHeight = fontMetrics.getHeight();
        int y = Math.min(y1, y2) + ((Math.abs(y2 - y1) - stringHeight) / 2);

        return y + fontMetrics.getAscent();
    }

    private void drawCenteredStringInRect(Graphics2D graphics, Font font, String string, int x1, int y1, int x2, int y2) {
        drawCenteredString(graphics, font, string, x1, x2, getCenteredY(graphics, font, y1, y2));
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
        if (!this.open)
            return;

        switch (key) {
            case "ESCAPE" -> {
                if (this.currentView.getParent() == null)
                    close();
                else {
                    this.currentView.setCurrentIndex(0);

                    setCurrentView(this.currentView.getParent());
                }
            }
            case "TAB" -> {
                if (this.currentView instanceof CategoryView categoryView) {
                    close();
                    GTAVTools.getToolManager().executeTool((Tool) this.currentComponents.get(categoryView.getCurrentIndex()));
                }
            }
            case "ENTER" -> {
                Component component = this.currentComponents.get(this.currentView.getCurrentIndex());

                if (component instanceof View view) {
                    if (!view.getComponents().isEmpty())
                        setCurrentView(view);
                }
            }
            case "DOWN" -> {
                this.currentView.setCurrentIndex((this.currentView.getCurrentIndex() + 1) % this.currentComponents.size());
                repaint();
            }
            case "UP" -> {
                this.currentView.setCurrentIndex((this.currentView.getCurrentIndex() - 1 +this.currentComponents.size()) % this.currentComponents.size());
                repaint();
            }
            case "RIGHT" -> {
                if (this.currentView instanceof Tool tool) {
                    ((Setting) this.currentComponents.get(tool.getCurrentIndex())).increase();
                    repaint();
                }
            }
            case "LEFT" -> {
                if (this.currentView instanceof Tool tool) {
                    ((Setting) this.currentComponents.get(tool.getCurrentIndex())).decrease();
                    repaint();
                }
            }
        }
    }

    public void setCurrentView(View view) {
        this.currentView = view;

        setCurrentComponents(view);
        repaint();
    }

    private void setCurrentComponents(View view) {
        this.currentComponents = sortByWidth(view.getComponents(), Component::getName);
    }

}
