package dev.justix.gtavtools.tools;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {

    CAYO_PERICO("Cayo Perico"),
    CASINO("Casino"),
    MISSION("Missions"),
    COMBAT("Combat"),
    LOBBY("Lobby"),
    MISC("Misc");

    final String displayName;

}
