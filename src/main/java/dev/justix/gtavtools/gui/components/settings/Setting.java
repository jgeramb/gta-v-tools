package dev.justix.gtavtools.gui.components.settings;

import dev.justix.gtavtools.gui.components.Component;
import dev.justix.gtavtools.tools.Tool;

public abstract class Setting extends Component {

    private final Object defaultValue;
    protected Object value;

    protected Setting(Tool parent, String name, Object defaultValue) {
        super(parent, name);

        this.defaultValue = defaultValue;
    }

    private Object getValue() {
        return (value == null) ? defaultValue : value;
    }

    public abstract void increase();

    public abstract void decrease();

    public Boolean booleanValue() {
        return (Boolean) getValue();
    }

    public Integer integerValue() {
        return (Integer) getValue();
    }

    public Double doubleValue() {
        return (Double) getValue();
    }


}
