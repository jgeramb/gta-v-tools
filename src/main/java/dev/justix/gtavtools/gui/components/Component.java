package dev.justix.gtavtools.gui.components;

import lombok.Getter;

@Getter
public class Component {

    private final View parent;
    private final String name;

    public Component(View parent, String name) {
        this.parent = parent;
        this.name = name;
    }

}
