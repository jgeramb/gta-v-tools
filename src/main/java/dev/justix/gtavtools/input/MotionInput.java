package dev.justix.gtavtools.input;

import lombok.Getter;

@Getter
public class MotionInput extends Input {

    private final int x, y;

    public MotionInput(InputType type, int x, int y) {
        super(type, 0);

        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Motion={x=" + x + ", y=" + y + "}";
    }

}
