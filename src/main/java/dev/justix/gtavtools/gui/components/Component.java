package dev.justix.gtavtools.gui.components;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Component {

    private final View parent;
    private final String name;

}
