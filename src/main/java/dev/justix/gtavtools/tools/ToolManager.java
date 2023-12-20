package dev.justix.gtavtools.tools;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.combat.AutoShoot;
import dev.justix.gtavtools.tools.lobby.SoloLobby;
import dev.justix.gtavtools.tools.lobby.SwitchInviteLobby;
import dev.justix.gtavtools.tools.lobby.SwitchPublicLobby;
import dev.justix.gtavtools.tools.misc.CallMechanic;
import dev.justix.gtavtools.tools.misc.RepairAllVehicles;
import dev.justix.gtavtools.tools.mission.*;

import java.util.HashMap;
import java.util.Map;


public class ToolManager {


    private final Logger logger;
    private final Map<Integer, Tool> activeTools;
    private int lastId;

    public ToolManager(Logger logger) {
        this.logger = logger;
        this.activeTools = new HashMap<>();
        this.lastId = 0;

        // Mission
        new ReplayGlitch(logger);
        new CollectLoot(logger);
        new DrainageGrate(logger);
        new FingerprintHack(logger);
        new PlasmaCutter(logger);
        new VaultCode(logger);
        new SignalBoxHack(logger);
        new StartCayoPerico(logger);

        // Combat
        new AutoShoot(logger);

        // Lobby
        new SoloLobby(logger);
        new SwitchPublicLobby(logger);
        new SwitchInviteLobby(logger);

        // Miscellaneous
        new CallMechanic(logger);
        new RepairAllVehicles(logger);
    }

    public void executeTool(Tool tool) {
        final int id = lastId = lastId + 1;

        new Thread(() -> {
            activeTools.put(id, tool);

            tool.setThreadName();

            try {
                tool.execute();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "An error occurred while executing tool:");

                //noinspection CallToPrintStackTrace
                ex.printStackTrace();
            } finally {
                activeTools.remove(id);
            }
        }).start();
    }

    public void forceStop() {
        activeTools.values().forEach(Tool::forceStop);
    }

}
