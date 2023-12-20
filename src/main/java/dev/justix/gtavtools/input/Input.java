package dev.justix.gtavtools.input;

import lombok.Getter;

@Getter
public class Input {

    private final InputType type;
    private final long value;

    public Input(InputType type, long value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Input={type=" + type.name() + ", value=" + value + "}";
    }

}
