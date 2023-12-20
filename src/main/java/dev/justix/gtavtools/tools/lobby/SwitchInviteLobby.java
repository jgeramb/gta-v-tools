package dev.justix.gtavtools.tools.lobby;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;
import dev.justix.gtavtools.util.SystemUtil;

public class SwitchInviteLobby extends Tool {

    public SwitchInviteLobby(Logger logger) {
        super(logger, Category.LOBBY, "Switch Invite-Only");
    }

    @Override
    public void execute() {
        logger.log(Level.INFO, "Switching lobbies to a new invite-only lobby...");

        InterfaceNavigationUtil.openPlayOnlineOptions(false);

        SystemUtil.keyPress("DOWN", 25);
        SystemUtil.sleep(150);

        SystemUtil.keyPress("ENTER", 25);
        SystemUtil.sleep(1500);

        SystemUtil.keyPress("ENTER", 25);
        SystemUtil.sleep(20);

        logger.log(Level.INFO, "Connecting to lobby");
    }

}
