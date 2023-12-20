package dev.justix.gtavtools.gui.components.settings;

import dev.justix.gtavtools.tools.Tool;

public class DoubleSetting extends Setting {

    private final double min, max, step;

    public DoubleSetting(Tool parent, String name, double defaultValue, double min, double max, double step) {
        super(parent, name, defaultValue);

        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public void increase() {
        if (doubleValue() >= (min + step))
            value = doubleValue() - step;
        else
            value = min;
    }

    @Override
    public void decrease() {
        if (doubleValue() <= (max - step))
            value = doubleValue() + step;
        else
            value = max;
    }

}
