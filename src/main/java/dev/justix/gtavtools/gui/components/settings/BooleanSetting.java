package dev.justix.gtavtools.gui.components.settings;

import dev.justix.gtavtools.tools.Tool;

public class BooleanSetting extends Setting {

    public BooleanSetting(Tool parent, String name, boolean defaultValue) {
        super(parent, name, defaultValue);
    }

    @Override
    public void increase() {
        toggle();
    }

    @Override
    public void decrease() {
        toggle();
    }

    public void toggle() {
        value = !(booleanValue());
    }

}
