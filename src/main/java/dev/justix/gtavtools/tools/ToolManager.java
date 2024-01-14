package dev.justix.gtavtools.tools;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.casino.KeypadHack;
import dev.justix.gtavtools.tools.casino.LaserDrill;
import dev.justix.gtavtools.tools.cayoperico.*;
import dev.justix.gtavtools.tools.combat.AutoShoot;
import dev.justix.gtavtools.tools.lobby.SoloLobby;
import dev.justix.gtavtools.tools.lobby.SwitchInviteLobby;
import dev.justix.gtavtools.tools.lobby.SwitchPublicLobby;
import dev.justix.gtavtools.tools.misc.*;
import dev.justix.gtavtools.tools.mission.ReplayGlitch;

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

        // Cayo Perico Heist
        new CollectLoot(logger);
        new DrainageGrate(logger);
        new dev.justix.gtavtools.tools.cayoperico.FingerprintHack(logger);
        new PlasmaCutter(logger);
        new SignalBoxHack(logger);
        new dev.justix.gtavtools.tools.cayoperico.StartMission(logger);
        new VaultCode(logger);

        // Diamond Casino Heist
        new LaserDrill(logger);
        new dev.justix.gtavtools.tools.casino.FingerprintHack(logger);
        new KeypadHack(logger);
        new dev.justix.gtavtools.tools.casino.StartMission(logger);

        // Mission
        new ReplayGlitch(logger);

        // Combat
        new AutoShoot(logger);

        // Lobby
        new SoloLobby(logger);
        new SwitchPublicLobby(logger);
        new SwitchInviteLobby(logger);

        // Miscellaneous
        new CallMechanic(logger);
        new CallMerryweather(logger);
        new CallPegasus(logger);
        new CopsLookAway(logger);
        new RemoveWantedLevel(logger);
        new RepairAllVehicles(logger);
    }

    public void executeTool(Tool tool) {
        final int id = this.lastId = this.lastId + 1;

        new Thread(() -> {
            this.activeTools.put(id, tool);

            tool.setThreadName();

            try {
                tool.execute();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "An error occurred while executing tool:");

                //noinspection CallToPrintStackTrace
                ex.printStackTrace();
            } finally {
                this.activeTools.remove(id);
            }
        }).start();
    }

    public void forceStop() {
        this.activeTools.values().forEach(Tool::forceStop);
    }

}
