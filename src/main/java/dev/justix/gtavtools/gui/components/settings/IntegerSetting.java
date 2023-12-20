package dev.justix.gtavtools.gui.components.settings;

import dev.justix.gtavtools.tools.Tool;

public class IntegerSetting extends Setting {

    private final int min, max, step;

    public IntegerSetting(Tool parent, String name, int defaultValue, int min, int max, int step) {
        super(parent, name, defaultValue);

        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public void increase() {
        if (integerValue() >= (min + step))
            value = integerValue() - step;
        else
            value = min;
    }

    @Override
    public void decrease() {
        if (integerValue() <= (max - step))
            value = integerValue() + step;
        else
            value = max;
    }

}
