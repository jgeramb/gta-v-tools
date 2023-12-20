package dev.justix.gtavtools.tools.lobby;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;
import dev.justix.gtavtools.util.SystemUtil;

public class SwitchPublicLobby extends Tool {

    public SwitchPublicLobby(Logger logger) {
        super(logger, Category.LOBBY, "Switch Public");
    }

    @Override
    public void execute() {
        logger.log(Level.INFO, "Switching lobbies to a new public lobby...");

        InterfaceNavigationUtil.openPlayOnlineOptions(false);

        SystemUtil.keyPress("ENTER", 25L);
        SystemUtil.sleep(1500L);

        SystemUtil.keyPress("ENTER", 25L);
        SystemUtil.sleep(20L);

        logger.log(Level.INFO, "Connecting to lobby");
    }

}
