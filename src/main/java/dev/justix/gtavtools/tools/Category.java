package dev.justix.gtavtools.tools;

import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
public enum Category {

    MISSION("Missions"),
    COMBAT("Combat"),
    LOBBY("Lobby"),
    MISC("Miscellaneous");

    final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public static Optional<Category> byDisplayName(String displayName) {
        return Stream.of(values()).filter(category -> category.getDisplayName().equals(displayName)).findAny();
    }

}
