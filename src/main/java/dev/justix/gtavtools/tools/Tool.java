package dev.justix.gtavtools.tools;

import dev.justix.gtavtools.GTAVTools;
import dev.justix.gtavtools.gui.components.View;
import dev.justix.gtavtools.gui.components.settings.Setting;
import dev.justix.gtavtools.logging.Logger;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Tool extends View {

    protected final Logger logger;
    @Getter
    private final Category category;
    private final Rectangle screenRect;
    private final List<Setting> settings;
    protected final RelativeToolData relativeData;

    protected Tool(Logger logger, Category category, String displayName) {
        super(GTAVTools.getGui().getMainView().getCategoryView(category), displayName);

        this.logger = logger;
        this.category = category;

        Dimension screenBounds = Toolkit.getDefaultToolkit().getScreenSize();

        this.screenRect = new Rectangle(0, 0, (int) Math.round(screenBounds.getWidth()), (int) Math.round(screenBounds.getHeight()));
        this.settings = new ArrayList<>();
        this.relativeData = new RelativeToolData();
    }

    public abstract void execute() throws Exception;

    public void forceStop() {
    }

    protected int screenWidth() {
        return (int) this.screenRect.getWidth();
    }

    protected int screenHeight() {
        return (int) this.screenRect.getHeight();
    }

    public void setThreadName() {
        Thread.currentThread().setName("Tool (" + getName() + ")");
    }

    @Override
    public List<Setting> getComponents() {
        return this.settings;
    }

    protected void addSetting(Setting setting) {
        this.settings.add(setting);
    }

    protected Boolean booleanValue(String settingName, boolean defaultValue) {
        return this.settings
                .stream()
                .filter(setting -> setting.getName().equals(settingName))
                .findFirst()
                .map(Setting::booleanValue)
                .orElse(defaultValue);
    }

}