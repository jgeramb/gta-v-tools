package dev.justix.gtavtools.tools.lobby;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.SystemUtil;

import java.io.IOException;

public class SoloLobby extends Tool {

    public SoloLobby(Logger logger) {
        super(logger, Category.LOBBY, "Solo");
    }

    @Override
    public void execute() throws IOException {
        logger.log(Level.INFO, "Pausing process for solo lobby creation...");

        Runtime.getRuntime().exec(new String[] { "pssuspend", "GTA5.exe" });

        SystemUtil.sleep(10 * 1000L);

        Runtime.getRuntime().exec(new String[] { "pssuspend", "-r", "GTA5.exe" });

        logger.log(Level.INFO, "Resuming process, solo lobby created");
    }

}
